package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillInfoTo {
    // 秒杀信息
    private Long id;
    /**
     *
     */
    private Long promotionId;
    /**
     *
     */
    private Long promotionSessionId;
    /**
     *
     */
    private Long skuId;
    /**
     *
     */
    private BigDecimal seckillPrice;
    /**
     *
     */
    private BigDecimal seckillCount;
    /**
     * ÿ
     */
    private BigDecimal seckillLimit;
    /**
     *
     */
    private Integer seckillSort;
//    /**
//     * 基本信息
//     */
//    private SkuInfoTo skuInfoTo;
    /**
     *  session 开始时间
     */
    private Long startTime;
    /**
     *  session 结束时间
     */
    private Long endTime;
    /**
     * 随机码
     */
    private String randomCode;
}
