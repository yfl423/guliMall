package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// 订单确认页所有要返回的数据
@ToString
public class OrderConfirmVo {
    @Getter
    @Setter
    List<MemberAddressVo> memberAddressVoList; // 收货地址
    @Getter
    @Setter
    List<OrderItemVo> orderItemVos; // 订单项<==>购物项
    @Getter
    @Setter
    private Integer integration; // 积分

    //    private BigDecimal totalPrice; // 订单总金额
    @Getter
    @Setter
    private BigDecimal discount = new BigDecimal(0); // 优惠金额

    //    private BigDecimal checkPrice; // 应付金额
    @Getter
    @Setter
    private String orderToken; //todo  防重令牌 防重复提交

    /**
     * 订单项的库存信息
     */
    @Getter
    @Setter
    private Map<Long,Boolean> stockInfo;


    public BigDecimal getTotalPrice() {
        BigDecimal total = new BigDecimal(0);
        if (orderItemVos != null && orderItemVos.size() > 0) {
            for (OrderItemVo orderItemVo : orderItemVos) {
                total = total.add(orderItemVo.getTotalPrice());
            }
        }
        return total;
    }

    public BigDecimal getCheckPrice() {
        return getTotalPrice().subtract(discount);
    }

    public Integer getTotalCount(){
        int totalCount = 0;
        if (orderItemVos != null && orderItemVos.size() > 0) {
            for (OrderItemVo orderItemVo : orderItemVos) {
                totalCount +=orderItemVo.getCount();
            }
        }
        return totalCount;
    }
}
