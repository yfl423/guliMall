package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareTo {
    private BigDecimal fare;
    @Deprecated
    private String receiver;
    @Deprecated
    private String address;

    private MemberAddressVo vo;
}
