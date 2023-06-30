package com.atguigu.gulimall.product.service.impl;


import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SeckillInfoTo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
//        SkuItemVo skuItemVo = new SkuItemVo();
//        // 封装vo的每个属性
//        // 1. sku基本信息获取
//        SkuInfoEntity skuInfoEntity = getById(skuId);
//        skuItemVo.setInfo(skuInfoEntity);
//        Long spuId = skuInfoEntity.getSpuId();
//        Long catalogId = skuInfoEntity.getCatalogId();
//        // 2. sku的图片信息
//        List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
//        skuItemVo.setImages(images);
//        // 3. spu的销售属性组合
//        List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
//        skuItemVo.setSaleAttrs(saleAttrs);
//        // 4. 获取spu的介绍
//        SpuInfoDescEntity spuDesc = spuInfoDescService.getById(spuId);
//        skuItemVo.setSpuDesc(spuDesc);
//        // 5. 获取spu的规格参数
//        List<SpuItemAttrGroup> attrGroupVos =  attrGroupService.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
//        skuItemVo.setGroupAttrs(attrGroupVos);
//        return skuItemVo;

        /**
         * 使用异步编排对业务进行优化：整个业务逻辑有5个步骤：其中步骤1，步骤2可以同时进行；步骤3，步骤4步骤5则需要依赖步骤1的结果返回来开启
         */
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> step1 = CompletableFuture.supplyAsync(() -> {
            // 1. sku基本信息获取
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);
        CompletableFuture<Void> step3 = step1.thenAcceptAsync((info) -> {
            // 3. spu的销售属性组合
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(info.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrs);
        }, executor);

        CompletableFuture<Void> step4 = step1.thenAcceptAsync((info) -> {
            // 4. 获取spu的介绍
            SpuInfoDescEntity spuDesc = spuInfoDescService.getById(info.getSpuId());
            skuItemVo.setSpuDesc(spuDesc);
        }, executor);

        CompletableFuture<Void> step5 = step1.thenAcceptAsync((info) -> {
            // 5. 获取spu的规格参数
            List<SpuItemAttrGroup> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(info.getSpuId(), info.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);


        CompletableFuture<Void> step2 = CompletableFuture.runAsync(() -> {
            // 2. sku的图片信息
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        CompletableFuture<Void> seckillInfoTask = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoTo seckillInfo = JSON.parseObject(JSON.toJSONString(r.get("data")), SeckillInfoTo.class);
                skuItemVo.setSeckillInfo(seckillInfo);
            }
        }, executor);

        CompletableFuture.allOf(step2, step3, step4, step5, seckillInfoTask).get();
        System.out.println("当前时间为："+ new Date().getTime());
        System.out.println("vo："+skuItemVo);
        return skuItemVo;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /**
         * key: '华为',//检索关键字
         * catelogId: 0,
         * brandId: 0,
         * min: 0,
         * max: 0
         */
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        // 关键字key搜索
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(q -> {
                q.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        // 支持品牌搜索
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && Long.parseLong(brandId) != 0) {
            queryWrapper.eq("brand_id", brandId);
        }
        // 支持分类搜索
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && Long.parseLong(catelogId) != 0) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        // 价格区间搜索
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        if ((!StringUtils.isEmpty(min)) || (!StringUtils.isEmpty(max))) {
            // 全等于0说明没设置价格区间，即不加此筛选条件
            if (!((new BigDecimal(min).compareTo(new BigDecimal("0")) == 0) && (new BigDecimal(max).compareTo(new BigDecimal("0")) == 0))) {
                queryWrapper.between("price", min, max);
            }
        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skus = baseMapper.selectList(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skus;
    }

}