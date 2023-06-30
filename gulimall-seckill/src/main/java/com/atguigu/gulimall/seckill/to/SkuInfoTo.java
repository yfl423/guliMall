package com.atguigu.gulimall.seckill.to;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class SkuInfoTo {
    /**
     * skuId
     */

    private Long skuId;
    /**
     * spuId
     */
    private Long spuId;
    /**
     * sku
     */
    private String skuName;
    /**
     * sku
     */
    private String skuDesc;
    /**
     *
     */
    private Long catalogId;
    /**
     * Ʒ
     */
    private Long brandId;
    /**
     * Ĭ
     */
    private String skuDefaultImg;
    /**
     *
     */
    private String skuTitle;
    /**
     *
     */
    private String skuSubtitle;
    /**
     *
     */
    private BigDecimal price;
    /**
     *
     */
    private Long saleCount;
}
