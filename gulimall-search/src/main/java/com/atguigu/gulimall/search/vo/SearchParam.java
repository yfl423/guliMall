package com.atguigu.gulimall.search.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 封装检索参数
 */
@Data
@ToString
public class SearchParam {
    /**
     * search.gulimall.com/list.html?catalog3Id=x&keyword=x&sort:xx_desc/asc&&attrs=xx_x:x:x&attrs=xx_x:x
     * 关键字 keyword
     * 分类id catalog3Id
     * 排序sort：salecount\hotScore\skuPrice desc/asc
     * 筛选条件：hasStock=0/1 。。。
     * 分页：
     */

    private String keyword;

    private Long catalog3Id;

    private String sort;

    private Integer hasStock;// 是否有货
    private String skuPrice; //价格区间
    private List<Long> brandId; //品牌id，可能多选 &brandId=x&brandId=x&brandId=x...
    private List<String> attrs; //商品属性，可能选多个属性进行筛选，某一个属性也可以同时选择多个值作为筛选条件 请求设计规范：attrs=xx_x:x:x&attrs=xx_x:x

    private Integer pageNum = 1; // 分页数据，默认第一页

    private String _queryString; // 原生的所有查询条件
}
