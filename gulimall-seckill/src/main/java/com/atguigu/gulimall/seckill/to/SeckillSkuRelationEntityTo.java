package com.atguigu.gulimall.seckill.to;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class SeckillSkuRelationEntityTo {
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
}
