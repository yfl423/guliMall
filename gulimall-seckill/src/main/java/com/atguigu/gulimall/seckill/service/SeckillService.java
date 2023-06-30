package com.atguigu.gulimall.seckill.service;

import com.atguigu.common.to.mq.QuickOrder;
import com.atguigu.gulimall.seckill.to.RelationEntityInRedisTo;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<RelationEntityInRedisTo> getseckillSkus();

    RelationEntityInRedisTo getSeckillInfo(Long skuId);

    QuickOrder kill(String killId, String code, Integer num);
}
