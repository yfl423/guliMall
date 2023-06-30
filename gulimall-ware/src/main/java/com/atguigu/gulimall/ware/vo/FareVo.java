package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    private BigDecimal fare;
    @Deprecated
    private String receiver;
    @Deprecated
    private String address;

    private MemeberReceiveAddressVo vo;
}
