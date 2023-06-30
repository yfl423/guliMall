package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
@ToString
@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    private String token;
    // 直接从购物车中查询商品信息，无需提交
    private BigDecimal checkPrice; // 提交后可以和从购物车中查询完的商品总价进行验价，如果总价不同可以提示用户
    private String note; // 还可以做备注
}
