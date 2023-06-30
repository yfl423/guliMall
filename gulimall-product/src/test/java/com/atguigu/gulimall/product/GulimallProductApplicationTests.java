package com.atguigu.gulimall.product;


import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void contextLoads() {
        System.out.println("helloworLd");
    }

    @Test
    public void testRedis() {
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        System.out.println(stringStringValueOperations.get("hello"));
    }

    @Test
    public void testRedisson() {
        RLock lock = redissonClient.getLock("my-lock");
        //阻塞式等待
        lock.lock();
        try {
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
    }

    @Test
    public void testdb() {
        List<SpuItemAttrGroup> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(6L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

    @Test
    public void testdb2() {
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(3L);
        System.out.println(saleAttrsBySpuId);
    }

    @Test
    public void testBloomFilter() {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("skuId");
        bloomFilter.tryInit(1000, 0.01);
        for (int i = 0; i < 1000; i++) {
            bloomFilter.add(i);
        }
        int res = 0;
        for (int i = 1001; i < 2001; i++) {
            if (bloomFilter.contains(i)) res++;
        }
        System.out.println(res);
    }

    @Test
    public void test05() {
//        Map<String, String> map = new HashMap<>();
//        map.put("price", "500");
//        map.put("info", "xxx");
//        redisTemplate.opsForHash().putAll("product:100", map);
    }

}






