package com.atguigu.common.to.mq;

import lombok.Data;

@Data
public class QuickOrder {
    String orderSn; // 订单号
    Long memberId; // 会员id
    String killId; // 秒杀商品id SessionId_SkuId;
    Integer num; // 秒杀数量
}
