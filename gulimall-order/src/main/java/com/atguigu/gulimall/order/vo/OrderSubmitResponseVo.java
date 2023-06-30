package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitResponseVo {
    private Integer code;
    private OrderEntity order;
}
