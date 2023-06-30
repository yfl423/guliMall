package com.atguigu.gulimall.order.to;

import java.math.BigDecimal;
import java.util.List;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Data;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> items;
    private BigDecimal checkPrice;
    private BigDecimal fare;
}
