package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1）保存spu的基本信息 （pms_spu_info）
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());// 补充vo中没有的属性
        spuInfoEntity.setUpdateTime(new Date());
        this.baseMapper.insert(spuInfoEntity);

        // 2）保存spu的描述图片 （pms_spu_info_desc）
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        Long spuId = spuInfoEntity.getId(); // 在保存时已经设置了generatedkey = ”id“，所以bean的属性值被数据库的自增主键注入
        descEntity.setSpuId(spuId);
        List<String> decript = vo.getDecript();
        descEntity.setDecript(String.join(",", decript)); // 将每条desc合成一个string，用”，“连接
        spuInfoDescService.getBaseMapper().insert(descEntity);

        // 3）保存spu的图片集 （pms_spu_images）
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuId, images);

        // 4）保存spu的规格参数 （pms_product_attr_value）
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity attrValueEntity = new ProductAttrValueEntity();
            Long attrId = baseAttr.getAttrId();
            attrValueEntity.setAttrId(attrId);
            String attrName = attrService.getById(attrId).getAttrName();
            attrValueEntity.setAttrName(attrName);
            attrValueEntity.setAttrValue(baseAttr.getAttrValues());
            attrValueEntity.setQuickShow(baseAttr.getShowDesc());
            attrValueEntity.setSpuId(spuId);
            return attrValueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveBatch(collect);
        // 5）保存spu的积分信息 (跨库操作)
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R r = couponFeignService.saveSpuBound(spuBoundTo);// openfeign 远程调用处理跨库保存
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }


        //  5）保存spu的sku信息
        // 5.1 保存sku的基本信息 （pms_sku_info）
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                // 获取defaultImg属性，它是在Image类的getDefaultImg==1维护
                String defaultImg = null;
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                // 一些属性是来自于spu
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.getBaseMapper().insert(skuInfoEntity);

                // 5.2 保存sku的图片信息 （pms_sku_images）
                List<Images> skuImages = item.getImages();
                List<SkuImagesEntity> skuImagesEntities = skuImages.stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(img, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId()); // generatedKey 主键自增
                    return skuImagesEntity;
                    // 前端提交的是所有图片，而我们需要保存的是被选中的，因此做一次过滤，将没有url的剔除掉
                }).filter(skuImagesEntity -> !StringUtils.isEmpty(skuImagesEntity.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);
// TODO 没有路径信息的图片无需保存
                // 5.3 保存sku的销售属性信息 （pms_sku_sale_attr_value）
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                // 5.4 保存sku的优惠、满减信息 （）
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                // 校验，确保远程调用的满减和打折是有意义的
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getReducePrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });


        }


    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
//        status:
//        key:
//        brandId: 0
//        catelogId: 0
        // 支持状态搜索
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        // 支持关键字查询
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(q -> {
                q.eq("id", key).or().like("spu_name", key);
            });
        }
        // 支持品牌搜索
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && Long.valueOf(brandId) != 0) {
            queryWrapper.eq("brand_id", brandId);
        }
        // 支持分类搜索
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && Long.valueOf(catelogId) != 0) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    /**
     * @param spuId
     */
    @Override
    public void spuUp(Long spuId) {

        // all skus have same following infos: brandId,catalogId,brandName,brandImg,catalogName,attrs
        // that is why they are queried firstly
        SpuInfoEntity spuInfoEntity = getById(spuId);
        Long brandId = spuInfoEntity.getBrandId();
        Long catalogId = spuInfoEntity.getCatalogId();
        BrandEntity brand = brandService.getById(brandId);
        String brandName = brand.getName();
        String brandImg = brand.getLogo();
        CategoryEntity catalog = categoryService.getById(catalogId);
        String catalogName = catalog.getName();
        // attrs (shift by search type)
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.getAttrsBySpuId(spuId);

        List<Long> attrIds = productAttrValueEntities.stream().map(entity -> entity.getAttrId()).collect(Collectors.toList());
        List<Long> selectAttrIds = attrService.selectSearchAttrs(attrIds);
        HashSet<Long> selectAttrIdsSet = new HashSet<>(selectAttrIds);

        List<SkuEsModel.Attrs> attrsList = productAttrValueEntities.stream().filter(item -> selectAttrIdsSet.contains(item.getAttrId())).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        //  ware system [open feign remote operation needed]
        // remote operation maybe unstable, so use try-catch
        Map<Integer, Boolean> stockInfo = new HashMap<>();
        try {
            List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            R stockResponse = wareFeignService.getSkuHasStock(skuIds);
            List<LinkedHashMap<String, Object>> data = (List<LinkedHashMap<String, Object>>) stockResponse.get("data");
            // TODO
            for (LinkedHashMap<String, Object> map : data) {
                Integer skuId = (Integer) map.get("skuId");
                Boolean hasStock = (Boolean) map.get("hasStock");
                stockInfo.put(skuId, hasStock);
            }
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }
        Map<Integer, Boolean> finalStockInfo = stockInfo;

        List<SkuEsModel> collect = skus.stream().map(skuInfoEntity -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
            // skuPrice,skuImg,hasStock,hotScore,brandName,brandImg,catalogName,attrs
            skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
            skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());

            //  hotScore set to be 0 [by default] should be controllable by back-end
            skuEsModel.setHotScore(0L);
            skuEsModel.setBrandName(brandName);
            skuEsModel.setBrandImg(brandImg);
            skuEsModel.setCatalogName(catalogName);
            skuEsModel.setAttrs(attrsList);
            if (finalStockInfo == null || finalStockInfo.size()==0) {
                skuEsModel.setHasStock(true);
            } else {
                Long skuId = skuEsModel.getSkuId();
                skuEsModel.setHasStock(finalStockInfo.get(skuId.intValue()));
            }
            return skuEsModel;
        }).collect(Collectors.toList());
        //  send data to es;
        R r = searchFeignService.productStatusUp(collect);
        if (r.getCode() == 0) {
            // remote operation succeed
            // TODO update status of spu
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // TODO 重复调用？接口幂等性？ 重试机制？
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        SpuInfoEntity spuInfoEntity = baseMapper.selectOne(new QueryWrapper<SpuInfoEntity>().eq("id", byId.getSpuId()));
        return spuInfoEntity;
    }


}