package com.atguigu.gulimall.ware.vo;

import lombok.Data;

@Data
public class WareSkuLockRespVo {
    private Boolean locked;
    private Integer num;
    private Long skuId;
}
