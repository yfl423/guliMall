package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<BrandEntity> page = null;
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<BrandEntity>().eq( "brand_id",key).or().like("name",key);
            page = this.page(
                    new Query<BrandEntity>().getPage(params),
                    queryWrapper
            );
        } else {
            page = this.page(
                    new Query<BrandEntity>().getPage(params),
                    new QueryWrapper<BrandEntity>()
            );
        }
        return new PageUtils(page);
    }

    /**
     * 更新自身时同步更新关联表的相关字段
     * @param brand
     */
    @Override
    public void updateAll(BrandEntity brand) {
        updateById(brand);
        if (StringUtils.isNotEmpty(brand.getName())){
            // 同时更新在分类品牌关联表中的brand.name信息
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
        }
    }

}