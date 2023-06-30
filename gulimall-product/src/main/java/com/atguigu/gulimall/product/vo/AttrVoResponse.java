package com.atguigu.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrVoResponse extends AttrVo {
    /**
     *
     */
    private String catelogName;
    /**
     *
     */
    private String attrGroupName;

    private Long [] catelogPath;


}
