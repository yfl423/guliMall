package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.RelationVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;

import java.util.Map;

/**
 * 
 *
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 22:43:05
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void batchDeleteByAttrAndAttrGroup(RelationVo[] relationVos);

    Boolean selectIfExist(Long attrId);

    void batchAddByAttrAndAttrGroup(RelationVo[] relationVos);
}

