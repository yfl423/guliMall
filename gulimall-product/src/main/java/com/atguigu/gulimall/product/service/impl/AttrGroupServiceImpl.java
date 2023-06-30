package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.RelationVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
        String key = (String) params.get("key");// 模糊查询的key
        // 没有点击指定分类id，默认传入的id为0，即查询所有
        if (categoryId == 0) {
            // 在所有项中按关键字查询
            if (StringUtils.isNotEmpty(key)) {
                QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", key);
                queryWrapper.or().like("attr_group_name", key);
                IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
                return new PageUtils(page);
            } else {
                // 即没有关键字，也没有指定catId，默认查询所有
                IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<AttrGroupEntity>());
                return new PageUtils(page);
            }

        } else {
            //在指定id下查所有该id的属性分类/额外如果有key也可以支持模糊查询
            // select * from pms_attr_group where catelog_id = categoryId (and attr_group_id =key or attr_group_name like key)
            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", categoryId);
            if (StringUtils.isNotEmpty(key)) {
                queryWrapper.and((obj) -> {
                    obj.eq("attr_group_id", key).or().like("attr_group_name", key);
                });
            }
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        }


    }

    @Override
    public List<AttrEntity> getAttrsByAttrgroupId(Long attrgroupId) {
        // 先根据分组id查询分组属性关联实体
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        // 根据属性id查询属性实体
        List<AttrEntity> attrs = relationEntities.stream().map(entity -> {
            Long attrId = entity.getAttrId();
            AttrEntity attrEntity = attrService.getById(attrId);
            return attrEntity;
        }).collect(Collectors.toList());
        return attrs;
    }

    @Override
    public void batchDeleteAttrgroupAndAttrRelation(RelationVo[] relationVos) {
        attrAttrgroupRelationService.batchDeleteByAttrAndAttrGroup(relationVos);
    }

    @Override
    public PageUtils getNonAttrsByAttrgroupId(Map<String, Object> params, Long attrgroupId) {
        // 当前分组只允许关联其所在分类中的所有属性
        // 1. 根据attrgroupId，查询其所在的catlogId;
        AttrGroupEntity attrGroupEntity = baseMapper.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 其所在分类下的所有分组（也包括自己）已经关联过的属性也不可以再被关联
        // 2.1 根据catelogId查询其他分组；
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(queryWrapper);
        List<Long> attrGroupIds = attrGroupEntities.stream().map(attrGroup -> attrGroup.getAttrGroupId()).collect(Collectors.toList());

        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()).and(attrQueryWrapper -> {
            attrQueryWrapper.eq("catelog_id", catelogId);
        });
        // 补充： 涉及in的list要做非空判断，否则会有异常
        if (attrGroupIds != null && attrGroupIds.size() > 0) {
            // 2.2 查询其他分组已经关联的属性；
            List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationService.getBaseMapper().selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIds));
            List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            if (attrIds != null && attrIds.size() > 0) {
                // 2.3 在当前分类的所有属性表中剔除这些属性(
                // i. 必须是base属性；(初始化已做判断)
                // ii. 必须是该分类下；(初始化已做判断)
                // iii. 剔除已被关联的属性；
                // )
                attrEntityQueryWrapper.notIn("attr_id", attrIds);
            }
            ;
        }
        // 如果有关键字，再支持模糊匹配
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            attrEntityQueryWrapper.and(w -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), attrEntityQueryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    @Override
    public void batchAddAttrgroupAndAttrRelation(RelationVo[] relationVos) {
        attrAttrgroupRelationService.batchAddByAttrAndAttrGroup(relationVos);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrgroupsWithAttrsVos(Long catelogId) {
        // 1) get attrgroups which belong to the catelog;
        List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // Nonempty check
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = null;
        if (attrGroupEntities != null && attrGroupEntities.size() > 0) {
            attrGroupWithAttrsVos = attrGroupEntities.stream().map(attrGroupEntity -> {
                AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
                BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrsVo);
                // 2) get attrs related to each attrgroup
                Long attrGroupId = attrGroupEntity.getAttrGroupId();
                List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = attrAttrgroupRelationService.getBaseMapper().selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
                List<Long> attrids = attrAttrgroupRelations.stream().map(attrAttrgroupRelation -> attrAttrgroupRelation.getAttrId()).collect(Collectors.toList());
                // again, nonempty check
                if (attrids != null && attrids.size() > 0) {
                    List<AttrEntity> attrEntities = attrService.getBaseMapper().selectList(new QueryWrapper<AttrEntity>().in("attr_id", attrids));
                    attrGroupWithAttrsVo.setAttrs(attrEntities);
                }
                return attrGroupWithAttrsVo;
            }).collect(Collectors.toList());
        }
        return attrGroupWithAttrsVos;
    }

    @Override
    public List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        List<SpuItemAttrGroup> groups = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        return groups;
    }


}