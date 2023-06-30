package com.atguigu.gulimall.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResVo;
import com.atguigu.gulimall.search.vo.BrandTo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResponse searchResponse = null;
        // 根据前端参数，封装es检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            searchResponse = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 将es响应封装为渲染页面的数据模型
        SearchResult searchResult = buildSearchResult(searchResponse, param);
        return searchResult;
    }

    private SearchResult buildSearchResult(SearchResponse searchResponse, SearchParam param) {
        SearchResult searchResult = new SearchResult();
        /**
         *  // 商品信息
         *     private List<SkuEsModel> products;
         *     // 分页信息
         *     private Integer PageNum;
         *     private Long total;
         *     private Integer totalPages;
         *     // 属性聚合信息
         *     private List<BrandVo> brands;
         *     private List<CatalogVo> catalogs;
         *     private List<AttrVo> attrs;
         */

        // 分页信息
        // 总记录数
        long total = searchResponse.getHits().getTotalHits().value;
        searchResult.setTotal(total);
        // 总页码
        Long totalPage = total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE : (total / EsConstant.PRODUCT_PAGESIZE + 1);
        searchResult.setTotalPages(Math.toIntExact(totalPage));
        // pageNav也根据总页码生成
        List<Integer> pageNav = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            pageNav.add(i);
        }
        searchResult.setPageNav(pageNav);
        searchResult.setPageNum(param.getPageNum());// 当前页码
        // 商品信息
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits != null && hits.length > 0) { // 检索有命中
            List<SkuEsModel> skuEsModels = Arrays.stream(hits).map(hit -> {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    // 如果有检索，说明有高亮，就将skuTitle替换为高亮
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                return skuEsModel;
            }).collect(Collectors.toList());
            searchResult.setProducts(skuEsModels);
        }
        // 聚合信息
        Aggregations aggregations = searchResponse.getAggregations();
        // catalogs聚合信息
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        if (catalog_agg != null) {
            List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
            List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
            buckets.forEach(bucket -> {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                catalogVo.setCatalogId(Long.valueOf(bucket.getKeyAsString()));
                Aggregations subAgg = bucket.getAggregations();
                ParsedStringTerms catalog_name_agg = subAgg.get("catalog_name_agg");
                String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
                catalogVo.setCatalogName(catalog_name);
                catalogVos.add(catalogVo);
            });
            searchResult.setCatalogs(catalogVos);
        }
        // brands聚合信息
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        if (brand_agg != null) {
            List<SearchResult.BrandVo> brandVos = new ArrayList<>();
            List<? extends Terms.Bucket> buckets = brand_agg.getBuckets();
            buckets.forEach(bucket -> {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                brandVo.setBrandId(Long.valueOf(bucket.getKeyAsString()));
                Aggregations subAgg = bucket.getAggregations();
                ParsedStringTerms brand_name_agg = subAgg.get("brand_name_agg");
                String brand_name = brand_name_agg.getBuckets().get(0).getKeyAsString();
                brandVo.setBrandName(brand_name);
                ParsedStringTerms brand_img_agg = subAgg.get("brand_img_agg");
                String catalog_img = brand_img_agg.getBuckets().get(0).getKeyAsString();
                brandVo.setBrandImg(catalog_img);
                brandVos.add(brandVo);
            });
            searchResult.setBrands(brandVos);
        }
        // attr 聚合
        ParsedNested attr_agg = aggregations.get("attr_agg");
        if (attr_agg != null) {
            List<SearchResult.AttrVo> attrVos = new ArrayList<>();
            ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
            List<? extends Terms.Bucket> buckets = attr_id_agg.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                attrVo.setAttrId(Long.valueOf(bucket.getKeyAsString()));
                Aggregations subAgg = bucket.getAggregations();
                ParsedStringTerms attr_name_agg = subAgg.get("attr_name_agg");
                String attr_name = attr_name_agg.getBuckets().get(0).getKeyAsString();
                attrVo.setAttrName(attr_name);
                ParsedStringTerms attr_value_agg = subAgg.get("attr_value_agg");
                List<String> attr_values = attr_value_agg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
                attrVo.setAttrValue(attr_values);
                attrVos.add(attrVo);
            }
            searchResult.setAttrs(attrVos);
        }
        // 构建面包屑导航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.valueOf(s[0]));
                // 同时把作为筛选条件的attrId存储起来
                searchResult.getAttrIds().add(Long.valueOf(s[0]));
                if (r.getCode() == 0) {
                    AttrResVo attrResVo = JSON.parseObject(JSON.toJSONString(r.get("attr")), new TypeReference<>() {
                    });
                    String attrName = attrResVo.getAttrName();
                    navVo.setNavName(attrName);
                } else {
                    // 远程调用失败
                    navVo.setNavName(s[0]); // 获取attr名称失败，暂用attrId代替
                }
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavVos(navVos);
        }
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navVos = searchResult.getNavVos();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.infos(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandTo> brands = JSON.parseObject(JSON.toJSONString(r.get("brand")), new TypeReference<>() {
                });
                StringBuffer stringBuffer = new StringBuffer();
                String replace = "";
                for (BrandTo brandTo : brands) {
                    stringBuffer.append(brandTo.getBrandName());
                    replace = replaceQueryString(param, brandTo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(stringBuffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            } else {
                    //
            }
            navVos.add(navVo);
        }
        return searchResult;
    }

    private String replaceQueryString(SearchParam param, String attr, String key) {
        //  nav link
        String encode = null;
        encode = URLEncoder.encode(attr, StandardCharsets.UTF_8); //中文的属性值会被字符编码
        encode = encode.replace("+", "%20"); // 浏览器对空格的编码和java不一样（浏览器会将空格的编码成“%20”，而java则是编码成“+”，所以要处理这个差异化）
        String replace = param.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }

    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder source = new SearchSourceBuilder();
        /**
         * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
         */

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            // 模糊匹配,全文检索
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("skuTitle", param.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
        }

        if (param.getCatalog3Id() != null) {
            // 分类id
            TermQueryBuilder catalogId = QueryBuilders.termQuery("catalogId", param.getCatalog3Id());
            boolQueryBuilder.filter(catalogId);
        }

        List<Long> brands = param.getBrandId();
        if (brands != null && brands.size() > 0) {
            //品牌id
            TermsQueryBuilder brandId = QueryBuilders.termsQuery("brandId", brands);
            boolQueryBuilder.filter(brandId);
        }
        List<String> attrs = param.getAttrs();
        // 属性attr
        if (attrs != null && attrs.size() > 0) {
            attrs.forEach(attr -> {
                String[] split = attr.split("_");
                String attrId = split[0];
                String[] attrValues = split[1].split(":");

                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("attrs.attrId", attrId);
                boolQuery.must(termQueryBuilder);
                TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("attrs.attrValue", attrValues);
                boolQuery.must(termsQueryBuilder);
                // 由于我们索引的映射设计，每个属性的过滤都封装成一个nested
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            });
        }

        // 是否有库存
        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 价格区间
        String skuPrice = param.getSkuPrice();
        if (!StringUtils.isEmpty(skuPrice)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] s = skuPrice.split("_");
            if (s.length == 2) {
                // 区间有下限也有上限
                rangeQueryBuilder.gte(s[0]).lte(s[1]);
            } else {
                // 区间只有上限
                if (skuPrice.startsWith("_")) {
                    rangeQueryBuilder.lte(s[0]);
                } else {
                    // 区间只有下限
                    rangeQueryBuilder.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);

        }
        source.query(boolQueryBuilder); // 至此 查询条件已经全部封装好
        /**
         * 排序，分页，高亮
         */
        String sort = param.getSort();
        if (!StringUtils.isEmpty(sort)) {
            // 排序
            String[] s = sort.split("_");
            source.sort(s[0], s[1].equalsIgnoreCase("desc") ? SortOrder.DESC : SortOrder.ASC);
        }
        // 分页
        Integer pageNum = param.getPageNum();
        int curr = (pageNum - 1) * EsConstant.PRODUCT_PAGESIZE;
        source.from(curr);
        source.size(EsConstant.PRODUCT_PAGESIZE);

        // 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            // 使用高亮的前提是有模糊匹配，否则没有意义
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            source.highlighter(highlightBuilder);
        }
        /**
         * 聚合特征
         */
        // 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 品牌子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        source.aggregation(brand_agg);
        // 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        // 分类子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        source.aggregation(catalog_agg);
        // attr聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        TermsAggregationBuilder attr_name = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        TermsAggregationBuilder attr_value = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50);
        attr_id.subAggregation(attr_name);
        attr_id.subAggregation(attr_value);
        attr_agg.subAggregation(attr_id);
        source.aggregation(attr_agg);

//        System.out.println(source.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, source);
        return searchRequest;
    }


}
