package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@ToString
@Data
public class OrderItemVo {
    private Long skuId;
    private String skuDefaultImg;
    private String skuTitle;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private List<String> skuAttr;
    // todo 重量
    private BigDecimal weight;
}
