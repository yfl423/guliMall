package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.vo.RelationVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * 
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 22:43:05
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void batchDeleteByAttrAndAttrGroup (@Param("relationVos") RelationVo[] relationVos);


    int selectIfExist(@Param(("attrId")) Long attrId);

    void batchAddByAttrAndAttrGroup(@Param("relationVos")  RelationVo[] relationVos);
}
