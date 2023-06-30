package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuInfoResponeVo {
    private static final long serialVersionUID = 1L;

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
