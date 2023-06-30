package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.atguigu.gulimall.product.vo.AttrVoResponse;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //将vo视图模型的属性注入到po持久化数据库模型中（属性名一定要完全相同）
        BeanUtils.copyProperties(attr, attrEntity);
        save(attrEntity);
        // 同时维护关联数据库
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) { // 因为销售属性不关联分组，所以仅维护规格参数属性
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrGroupId(attr.getAttrGroupId());
            relation.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationService.save(relation);
        }
    }

    @Override
    public PageUtils queryBasePage(Map<String, Object> params, String type, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type",
                ("base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode()));
        if (catelogId != 0) {
            queryWrapper.eq("catelog_Id", catelogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(obj -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrVoResponse> attrVoResponses = records.stream().map(attrEntity -> {
            AttrVoResponse attrVoResponse = new AttrVoResponse();
            BeanUtils.copyProperties(attrEntity, attrVoResponse);
            Long id = attrVoResponse.getCatelogId();
            String catelogName = categoryService.getNameById(id);
            attrVoResponse.setCatelogName(catelogName);

            if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
                Long attrId = attrEntity.getAttrId();
                AttrAttrgroupRelationEntity relation = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
                // attrGroupId
                if (relation != null) {
                    Long attrGroupId = relation.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
                    if (attrGroupEntity != null) {
                        // attrGroupName
                        String attrGroupName = attrGroupEntity.getAttrGroupName();
                        attrVoResponse.setAttrGroupId(attrGroupId);
                        attrVoResponse.setAttrGroupName(attrGroupName);
                    }
                }
            }
            return attrVoResponse;
        }).collect(Collectors.toList());
        pageUtils.setList(attrVoResponses);
        return pageUtils;
    }

    @Cacheable(value = "attr", key = "'getDetailsById:'+#root.args[0]")
    @Override
    public AttrVoResponse getDetailsById(Long attrId) {
        AttrVoResponse attrVoResponse = new AttrVoResponse();
        AttrEntity attrEntity = getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrVoResponse);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 1.设置分组信息
            AttrAttrgroupRelationEntity relation = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relation != null) {
                Long attrGroupId = relation.getAttrGroupId();
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
                if (attrGroupEntity != null) {
                    String attrGroupName = attrGroupEntity.getAttrGroupName();
                    attrVoResponse.setAttrGroupId(attrGroupId);
                    attrVoResponse.setAttrGroupName(attrGroupName);
                }
            }
        }

        // 2.设置分类信息
        Long catelogId = attrVoResponse.getCatelogId();
        String categoryName = categoryService.getNameById(catelogId);
        Long[] catelogPath = categoryService.getCatelogPathByCatelogId(catelogId);
        attrVoResponse.setCatelogName(categoryName);
        attrVoResponse.setCatelogPath(catelogPath);
        return attrVoResponse;
    }

    @Transactional
    @Override
    public void updatAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            Long attrId = attr.getAttrId();
            Long attrGroupId = attr.getAttrGroupId();
            // 修改分组关联
            // 情况一：relation中有此关联信息，此时修改分组关联是update
            // 情况二：relation中没有此关联信息(被某些操作删除)，此时修改分组关联实际是insert（新增）
            // 因此要先通过查询判断是哪种情况

            Boolean exist = attrAttrgroupRelationService.selectIfExist(attrId);
            if (exist) {
                // update
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                relationEntity.setAttrId(attrId);
                relationEntity.setAttrGroupId(attrGroupId);
                attrAttrgroupRelationService.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            } else {
                // insert
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                relationEntity.setAttrGroupId(attrGroupId);
                relationEntity.setAttrId(attrId);
                attrAttrgroupRelationService.save(relationEntity);
            }
        }
    }

    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {
        return baseMapper.selectSearchAttrs(attrIds);
    }

}