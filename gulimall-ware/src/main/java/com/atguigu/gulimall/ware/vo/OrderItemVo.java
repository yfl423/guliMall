package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderItemVo {
    private Long id;
    /**
     * order_id
     */
    private Long orderId;
    /**
     * order_sn
     */
    private String orderSn;
    /**
     * spu_id
     */
    private Long spuId;
    /**
     * spu_name
     */
    private String spuName;
    /**
     * spu_pic
     */
    private String spuPic;
    /**
     *
     */
    private String spuBrand;
    /**
     * id
     */
    private Long categoryId;
    /**
     * sku
     */
    private Long skuId;
    /**
     * sku
     */
    private String skuName;
    /**
     * sku
     */
    private String skuPic;
    /**
     * sku
     */
    private BigDecimal skuPrice;
    /**
     *
     */
    private Integer skuQuantity;
    /**
     * JSON
     */
    private String skuAttrsVals;
    /**
     *
     */
    private BigDecimal promotionAmount;
    /**
     *
     */
    private BigDecimal couponAmount;
    /**
     *
     */
    private BigDecimal integrationAmount;
    /**
     *
     */
    private BigDecimal realAmount;
    /**
     *
     */
    private Integer giftIntegration;
    /**
     *
     */
    private Integer giftGrowth;

}
