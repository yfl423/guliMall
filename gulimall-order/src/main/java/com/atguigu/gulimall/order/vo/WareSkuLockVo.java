package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderItemEntity;
import java.util.List;
import lombok.Data;

@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemEntity> items;
}
