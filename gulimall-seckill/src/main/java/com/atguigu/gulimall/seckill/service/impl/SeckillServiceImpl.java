package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.MemberTo;
import com.atguigu.common.to.mq.QuickOrder;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignClient;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.RelationEntityInRedisTo;
import com.atguigu.gulimall.seckill.to.SeckillSessionTo;
import com.atguigu.gulimall.seckill.to.SeckillSkuRelationEntityTo;
import com.atguigu.gulimall.seckill.to.SkuInfoTo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignClient couponFeignClient;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    private static final String SECKILL_SESSION_CACHE_KEY_PREFIX = "seckill:sessions:";

    private static final String SECKILL_SKURELATION_CACHE_KEY_PREFIX = "seckill:skus";

    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Override
    public void uploadSeckillSkuLatest3Days() {
        String startTime = getStartTime();
        String endTime = getEndTime();
        // 根据当前的时间信息在优惠系统中远程查询最近3天的秒杀场次信息
        R r = couponFeignClient.get3LatestDaysSession(startTime, endTime);
        if (r.getCode() == 0) {
            List<SeckillSessionTo> sessionTos = JSON.parseObject(JSON.toJSONString(r.get("data")), new TypeReference<List<SeckillSessionTo>>() {
            });
            if (sessionTos != null && sessionTos.size() > 0) {
                // 缓存到redis
                sessionTos.forEach(sessionData -> {
                    // 1. 保存seckill_session
                    saveSeckillSession(sessionData);
                    // 2. 保存每个session关联的sku
                    saveSkuRelation(sessionData);
                });
            }
        }
    }

    @Override
    public List<RelationEntityInRedisTo> getseckillSkus() {
        List<RelationEntityInRedisTo> res = new ArrayList<>();
        Set<String> sessionKeys = redisTemplate.keys(SECKILL_SESSION_CACHE_KEY_PREFIX + "*");
        if (sessionKeys != null && sessionKeys.size() > 0) {
            for (String sessionKey : sessionKeys) {
                String replace = sessionKey.replace(SECKILL_SESSION_CACHE_KEY_PREFIX, "");
                String[] s = replace.split("_");
                long time = new Date().getTime();
                if (time > Long.parseLong(s[0]) && time < Long.parseLong(s[1])) {
                    // 当前时间处于该session的活动时间,获取该session关联的所有sku的key
                    List<String> range = redisTemplate.opsForList().range(sessionKey, -100, 100);
                    if (range != null && range.size() > 0) {
                        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_SKURELATION_CACHE_KEY_PREFIX);
                        // 获取该session关联的所有sku
                        List<String> skuList = ops.multiGet(range);
                        if (skuList != null && skuList.size() > 0) {
                            skuList.forEach(jsonString -> {
                                RelationEntityInRedisTo relationEntityInRedisTo = JSON.parseObject(jsonString, RelationEntityInRedisTo.class);
                                res.add(relationEntityInRedisTo);
                            });
                        }
                    }
                }
            }
        }
        return res;
    }

    @Override
    public RelationEntityInRedisTo getSeckillInfo(Long skuId) {
        List<String> res = new ArrayList<>();
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_SKURELATION_CACHE_KEY_PREFIX);
        String regex = "\\d_" + skuId;
        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                // 使用正则表达式进行匹配
                if (Pattern.matches(regex, key)) {
                    String s = ops.get(key);
                    res.add(s);
                }
            }
            List<RelationEntityInRedisTo> collect = res.stream().map(jsonString -> JSON.parseObject(jsonString, RelationEntityInRedisTo.class)).collect(Collectors.toList());
            if (collect.size() > 0) {
                if (collect.size() > 1) {
                    // 一种可能的情况是某件商品处在多个秒杀session中，所以我们要过滤已经结束的，并且排序最靠近当前时间的
                    collect = collect.stream().filter(item -> item.getEndTime() > new Date().getTime()).collect(Collectors.toList());
                    collect.sort((i1, i2) -> (int) (i1.getStartTime() - i2.getStartTime()));
                }
                RelationEntityInRedisTo redisTo = collect.get(0);
                if (redisTo.getStartTime() >= new Date().getTime()) {
                    // 如果它的活动时间还没开始 就把随机码置空
                    redisTo.setRandomCode("");
                }
                return redisTo;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public QuickOrder kill(String killId, String code, Integer num) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_SKURELATION_CACHE_KEY_PREFIX);
        if (ops.hasKey(killId)) {
            String s = ops.get(killId);
            RelationEntityInRedisTo to = JSON.parseObject(s, RelationEntityInRedisTo.class);
            // 在生成订单之前，要进行一系列的校验 (合法性校验)
            long current = new Date().getTime();
            if (current > to.getStartTime() && current < to.getEndTime()) {
                // 1. 当前商品是否处于秒杀活动时间内
                String randomCode = to.getRandomCode();
                if (randomCode.equals(code)) {
                    // 2. 随机码校验
                    MemberTo memberTo = LoginInterceptor.threadLocal.get();
                    Long id = memberTo.getId();
                    String key = id + "_" + to.getPromotionSessionId() + "_" + to.getSkuId(); // 用户id_活动场此id_商品id
                    long ttl = to.getEndTime() - current; // 占位有效期与活动到期时间一直即可
                    Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(key, num.toString(), ttl, TimeUnit.MILLISECONDS);
                    // 3. 幂等性的校验和维护 （即要校验该用户是否已购买过）
                    if (aBoolean) {
                        int seckillLimit = to.getSeckillLimit().intValue();
                        if (num <= seckillLimit) {
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + code);
                            // 4. 信号量控制流量
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if (b) {
                                    // 秒杀成功，准备订单
                                    String orderSn = IdWorker.get32UUID().substring(0, 6); // 订单号
                                    Long memberId = LoginInterceptor.threadLocal.get().getId(); // 会员id
                                    QuickOrder quickOrder = new QuickOrder();
                                    quickOrder.setKillId(killId);
                                    quickOrder.setNum(num);
                                    quickOrder.setMemberId(memberId);
                                    quickOrder.setOrderSn(orderSn);
                                    return quickOrder;

                                }
                            } catch (InterruptedException e) {
                                return null;
                            }

                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 保存seckill_session
     *
     * @param seckillSessionTo
     */
    private void saveSeckillSession(SeckillSessionTo seckillSessionTo) {
        long createTime = seckillSessionTo.getCreateTime().getTime();
        long eTime = seckillSessionTo.getEndTime().getTime();
        String sessionKey = SECKILL_SESSION_CACHE_KEY_PREFIX + createTime + "_" + eTime;
        // todo 幂等性操作
        if (!redisTemplate.hasKey(sessionKey)) {
            List<SeckillSkuRelationEntityTo> skuRelationEntities = seckillSessionTo.getSkuRelationEntities();
            if (skuRelationEntities != null && skuRelationEntities.size() > 0) {
                List<String> collect = skuRelationEntities.stream().map(seckillSkuRelationEntityTo -> seckillSkuRelationEntityTo.getPromotionSessionId() + "_" + seckillSkuRelationEntityTo.getSkuId()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(sessionKey, collect);
            }
        }

    }

    /**
     * 保存每个session关联的sku模型(秒杀信息和原始商品信息和随机码)
     *
     * @param seckillSessionTo
     */
    private void saveSkuRelation(SeckillSessionTo seckillSessionTo) {
        // 2. 保存每个session中的skuRelation信息
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_SKURELATION_CACHE_KEY_PREFIX);
        List<SeckillSkuRelationEntityTo> skuRelationEntities = seckillSessionTo.getSkuRelationEntities();
        if (skuRelationEntities != null && skuRelationEntities.size() > 0) {
            // 远程调用product服务接口，根据skuId批量查询商品的详细信息
            List<Long> skuIds = skuRelationEntities.stream().map(SeckillSkuRelationEntityTo::getSkuId).collect(Collectors.toList());
            R r = productFeignService.infos(skuIds);
            List<SkuInfoTo> data = JSON.parseObject(JSON.toJSONString(r.get("data")), new TypeReference<>() {
            });
            Map<Long, SkuInfoTo> skuBasicInfos = data.stream().collect(Collectors.toMap(SkuInfoTo::getSkuId, skuInfoTo -> skuInfoTo));

            skuRelationEntities.forEach(seckillSkuRelationEntityTo -> {
                if (!ops.hasKey(seckillSkuRelationEntityTo.getPromotionSessionId() + "_" + seckillSkuRelationEntityTo.getSkuId())) {
                    //  保存在redis的sku模型
                    RelationEntityInRedisTo relationEntityInRedisTo = new RelationEntityInRedisTo();
                    // 1. 封装秒杀信息
                    BeanUtils.copyProperties(seckillSkuRelationEntityTo, relationEntityInRedisTo);
                    // 2. 封装原始商品信息
                    relationEntityInRedisTo.setSkuInfoTo(skuBasicInfos.get(relationEntityInRedisTo.getSkuId()));
                    //  3. 封装session的开始和结束时间
                    relationEntityInRedisTo.setStartTime(seckillSessionTo.getStartTime().getTime());
                    relationEntityInRedisTo.setEndTime(seckillSessionTo.getEndTime().getTime());
                    // 4. todo 随机码 （保护机制）
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    relationEntityInRedisTo.setRandomCode(randomCode);
                    // 引入信号量作为流量控制
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(relationEntityInRedisTo.getSeckillCount().intValue());

                    String s = JSON.toJSONString(relationEntityInRedisTo);
                    ops.put(relationEntityInRedisTo.getPromotionSessionId() + "_" + relationEntityInRedisTo.getSkuId(), s);
                }
            });
        }
    }


    /**
     * 计算当前时间  format: yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    private String getStartTime() {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.of(0, 0, 0);
        LocalDateTime startTime = LocalDateTime.of(date, time);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    }

    private String getEndTime() {
        LocalDate now = LocalDate.now();
        LocalDate date = now.plusDays(2L);
        LocalTime time = LocalTime.of(23, 59, 59);
        LocalDateTime endTime = LocalDateTime.of(date, time);
        return endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
