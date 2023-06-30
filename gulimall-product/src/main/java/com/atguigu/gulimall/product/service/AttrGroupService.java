package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.RelationVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 22:43:05
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    List<AttrEntity> getAttrsByAttrgroupId(Long attrgroupId);

    void batchDeleteAttrgroupAndAttrRelation(RelationVo[] relationVos);

    PageUtils getNonAttrsByAttrgroupId(Map<String, Object> params,Long attrgroupId);

    void batchAddAttrgroupAndAttrRelation(RelationVo[] relationVos);


    List<AttrGroupWithAttrsVo> getAttrgroupsWithAttrsVos(Long catelogId);

    List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

