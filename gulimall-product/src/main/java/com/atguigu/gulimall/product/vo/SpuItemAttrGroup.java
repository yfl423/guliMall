package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpuItemAttrGroup {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;

}
