package com.atguigu.gulimall.search.vo;

import java.util.ArrayList;
import java.util.List;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class SearchResult {
    // 商品信息
    private List<SkuEsModel> products;
    // 分页信息
    private Integer PageNum; // 当前页码
    private Long total; // 总记录数
    private Integer totalPages; // 总页码
    private List<Integer> pageNav; //导航页用于分页

    private List<BrandVo> brands;
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;

    private List<NavVo> navVos = new ArrayList<>(); // 面包屑导航功能

    private List<Long> attrIds = new ArrayList<>(); // 记录已作为筛选条件的attr，这样前端的聚合部分就不必显示

    @Data
    public static class NavVo { // 面包屑模型
        String navName; // 面包屑展示的attr name
        String navValue; // 面包屑展示的attr value
        String link; // 取消这个面包屑之后，跳转到的link
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @ToString
    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

}
