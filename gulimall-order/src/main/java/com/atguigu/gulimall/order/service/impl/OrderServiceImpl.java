package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.to.MemberTo;
import com.atguigu.common.to.mq.OrderEntityTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignClient;
import com.atguigu.gulimall.order.feign.MemberFeignClient;
import com.atguigu.gulimall.order.feign.ProductFeignClient;
import com.atguigu.gulimall.order.feign.WareFeignClient;
import com.atguigu.gulimall.order.interceptor.LoginInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.to.FareTo;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.to.SpuInfoTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignClient memberFeignClient;
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    WareFeignClient wareFeignClient;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public static ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberTo memberTo = LoginInterceptor.threadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();// 异步下threadlocal无法共享，所以要把requestAttributes放到每个异步中
        // 远程查询 address 信息
        CompletableFuture<Void> addressInfoTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressInfo = memberFeignClient.getAddressInfo(memberTo.getId());
            orderConfirmVo.setMemberAddressVoList(addressInfo);
        }, executor);

        // 远程查询 orderItem 信息
        CompletableFuture<Void> orderItemsTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> itemList = cartFeignClient.getAllCheckedItems();
            if (itemList == null || itemList.size() == 0) return;
            orderConfirmVo.setOrderItemVos(itemList);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> orderItemVos = orderConfirmVo.getOrderItemVos();
            if (orderItemVos != null && orderItemVos.size() > 0) {
                List<Long> skuIds = orderItemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                R r = wareFeignClient.getSkuHasStock(skuIds);
                Object data = r.get("data");
                List<StockInfoVo> infos = JSON.parseObject(JSON.toJSONString(data), new TypeReference<List<StockInfoVo>>() {
                });
                Map<Long, Boolean> stockInfo = infos.stream().collect(Collectors.toMap(StockInfoVo::getSkuId, StockInfoVo::isHasStock));
                orderConfirmVo.setStockInfo(stockInfo);
            }
        }, executor);

        // 查询用户积分
        Integer integration = memberTo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // todo 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(token);
        String tokenKey = OrderConstant.ORDER_TOKEN_PREFIX + memberTo.getId();
        redisTemplate.opsForValue().set(tokenKey, token, 30, TimeUnit.MINUTES);

        CompletableFuture.allOf(addressInfoTask, orderItemsTask).get();
        return orderConfirmVo;
    }

    @Transactional
    @Override
    public OrderSubmitResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        orderSubmitVoThreadLocal.set(orderSubmitVo);
        OrderSubmitResponseVo responseVo = new OrderSubmitResponseVo();
        responseVo.setCode(0);
        // 1. 验证令牌
        String tokenFromClient = orderSubmitVo.getToken();
        MemberTo memberTo = LoginInterceptor.threadLocal.get();
        // 令牌机制的核心在于令牌的查，验，删必须是一个原子性操作！
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long validResult = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList(OrderConstant.ORDER_TOKEN_PREFIX + memberTo.getId()), tokenFromClient);
        if (validResult == 0) {
            // 验证失败
            responseVo.setCode(BizCodeEnume.ORDER_REPEAT_SUBMIT_EXCEPTION.getCode());
        } else {
            // 验证成功
            OrderCreateTo orderCreateTo = createOrder();
            // 2. 验价
            BigDecimal priceFromPage = orderCreateTo.getCheckPrice();
            BigDecimal priceFromCart = orderSubmitVo.getCheckPrice();
            BigDecimal diff = priceFromPage.subtract(priceFromCart);
            if (Math.abs(diff.intValue()) >= 0.01) {
                // 验价失败
                responseVo.setCode(BizCodeEnume.PRICE_NOT_SAME_EXCEPTION.getCode());
            } else {
                // 验价成功
                // 3. 保存订单
                saveOrder(orderCreateTo);
                // 4. 锁定库存,只要有异常，回滚订单数据
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                wareSkuLockVo.setItems(orderCreateTo.getItems());
                R r = wareFeignClient.lockStockAfterOrder(wareSkuLockVo);
                if (r.getCode() == 0) {
                    // 库存锁定成功
                    responseVo.setOrder(orderCreateTo.getOrder());
                    // todo 清空购物车
                } else {
                    // 库存锁定失败
                    responseVo.setCode(r.getCode());
//                    throw NoStockException();
                }

                // 5. 模拟减积分失败
//                int i = 10 / 0;
                // 提交订单成功后，发送订单的消息
                rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderCreateTo.getOrder());
            }
        }
        return responseVo;
    }

    @Override
    public void closerOrder(OrderEntity orderEntity) {
        // 在执行关单前要先进行判断，因为所有订单的产生都会发消息，只有到期未支付的订单才被关闭
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //进行关单（实质就是更改该订单的状态）
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            baseMapper.updateById(update); // 一旦订单状态转变为已取消，那么接下来对应的解锁库存操作就会执行

            OrderEntityTo orderEntityTo = new OrderEntityTo();
            BeanUtils.copyProperties(orderEntity, orderEntityTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderEntityTo); // 主动引发的解锁库存逻辑，即订单关闭后，就要解锁已预留的库存
        }
    }

    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        baseMapper.insert(order);
        List<OrderItemEntity> items = orderCreateTo.getItems();
        orderItemService.saveBatch(items);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        OrderEntity orderEntity = buildOrder();
        List<OrderItemEntity> itemEntities = buildOrderItems(orderEntity.getOrderSn());

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setItems(itemEntities);
        computePrice(orderEntity, itemEntities);

        orderCreateTo.setCheckPrice(orderEntity.getPayAmount());
        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal(0);
        BigDecimal couponAmount = new BigDecimal(0);
        BigDecimal integrationAmount = new BigDecimal(0);
        BigDecimal promotionAmount = new BigDecimal(0);

        BigDecimal gift = new BigDecimal(0);
        BigDecimal growth = new BigDecimal(0);
        for (OrderItemEntity entity : itemEntities) {
            BigDecimal realAmount = entity.getRealAmount();
            total = total.add(realAmount);
            couponAmount = couponAmount.add(entity.getCouponAmount());
            integrationAmount = integrationAmount.add(entity.getIntegrationAmount());
            promotionAmount = promotionAmount.add(entity.getPromotionAmount());
            gift = gift.add(new BigDecimal(entity.getGiftIntegration()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth()));
        }
        // 应付总额
        orderEntity.setTotalAmount(total);
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPayAmount(orderEntity.getTotalAmount().add(orderEntity.getFreightAmount()));

        // 积分信息
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0);
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemEntity> orderItems = new ArrayList<>();
        List<OrderItemVo> cartItems = cartFeignClient.getAllCheckedItems();
        if (cartItems != null && cartItems.size() > 0) {
            orderItems = cartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                // spu信息
                R r = productFeignClient.getSpuInfoBySkuId(cartItem.getSkuId());
                SpuInfoTo spuInfo = JSON.parseObject(JSON.toJSONString(r.get("data")), SpuInfoTo.class);
                orderItemEntity.setSpuId(spuInfo.getId());
                orderItemEntity.setSpuName(spuInfo.getSpuName());
                orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
                orderItemEntity.setCategoryId(spuInfo.getCatalogId());
                // sku信息
                orderItemEntity.setOrderSn(orderSn);
                orderItemEntity.setSkuId(cartItem.getSkuId());
                orderItemEntity.setSkuName(cartItem.getSkuTitle());
                orderItemEntity.setSkuPic(cartItem.getSkuDefaultImg());
                orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";"));
                orderItemEntity.setSkuQuantity(cartItem.getCount());
                orderItemEntity.setSkuPrice(cartItem.getPrice());
                // 积分信息
                orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity())).intValue());
                orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity())).intValue());
                // 价格信息
                orderItemEntity.setPromotionAmount(new BigDecimal(0));
                orderItemEntity.setCouponAmount(new BigDecimal(0));
                orderItemEntity.setIntegrationAmount(new BigDecimal(0));

                BigDecimal realCount = orderItemEntity.getSkuPrice()
                        .multiply(new BigDecimal(orderItemEntity.getSkuQuantity()))
                        .subtract(orderItemEntity.getPromotionAmount())
                        .subtract(orderItemEntity.getIntegrationAmount())
                        .subtract(orderItemEntity.getCouponAmount());
                orderItemEntity.setRealAmount(realCount);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return orderItems;
    }

    private OrderEntity buildOrder() {
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        MemberTo memberTo = LoginInterceptor.threadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        // 会员id
        orderEntity.setMemberId(memberTo.getId());
        // 唯一订单号
        String orderSn = IdWorker.get32UUID().substring(0, 6);
        orderEntity.setOrderSn(orderSn);
        Long addrId = orderSubmitVo.getAddrId();
        R r = wareFeignClient.getFare(addrId);
        FareTo fareTo = JSON.parseObject(JSON.toJSONString(r.get("data")), FareTo.class);
        // 设置运费信息
        orderEntity.setFreightAmount(fareTo.getFare());
        MemberAddressVo addr = fareTo.getVo();
        // 设置收货人信息
        orderEntity.setReceiverCity(addr.getCity());
        orderEntity.setReceiverDetailAddress(addr.getDetailAddress());
        orderEntity.setReceiverName(addr.getName());
        orderEntity.setReceiverPhone(addr.getPhone());
        orderEntity.setReceiverPostCode(addr.getPostCode());
        orderEntity.setReceiverProvince(addr.getProvince());
        orderEntity.setReceiverRegion(addr.getRegion());
        // 订单的相关状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        // todo 收货日期
        return orderEntity;
    }


}