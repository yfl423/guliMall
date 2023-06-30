package com.atguigu.gulimall.seckill.controller;

import java.util.List;

import com.atguigu.common.to.mq.QuickOrder;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.RelationEntityInRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class SeckillController {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 获取所有在当前时间进行秒杀的商品，用于处理加载检索首页时前端发来的ajax请求，获取当前正在进行秒杀活动商品
     * // todo 这个接口在秒杀开始的时候会不会压力很大
     *
     * @return
     */
    @GetMapping("/seckillSkus")
    public R getseckillSkus() {
        List<RelationEntityInRedisTo> skus = seckillService.getseckillSkus();
        return R.ok().put("data", skus);
    }

    /**
     * 获取指定skuid的秒杀信息，用于在商品详情页，展示当前商品是否有秒杀活动
     *
     * @param skuId
     * @return
     */
    @GetMapping("/seckill/{skuId}")
    public R getSeckillInfo(@PathVariable("skuId") Long skuId) {
        RelationEntityInRedisTo seckillInfo = seckillService.getSeckillInfo(skuId);
        return R.ok().put("data", seckillInfo);
    }

    /**
     * 秒杀请求
     * @param killId SessionId_SkuId
     * @param code   随机码
     * @param num    购买的数量
     * @return
     */
    @GetMapping("/kill")
    public R seckill(@RequestParam("killId") String killId, @RequestParam("key") String code, @RequestParam("num") Integer num) {
        QuickOrder quickOrder = seckillService.kill(killId, code, num);
        rabbitTemplate.convertAndSend("order-exchange", "quick_order_queue", quickOrder);
        return R.ok().put("data", quickOrder);
    }
}
