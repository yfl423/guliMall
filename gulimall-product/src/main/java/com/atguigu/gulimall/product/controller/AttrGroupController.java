package com.atguigu.gulimall.product.controller;

import java.util.List;
import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.RelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 23:22:52
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long categoryId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, categoryId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.getCatelogPathByCatelogId(catelogId);
//        System.out.println(Arrays.toString(catelogPath));
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 查询已关联信息
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> attrs = attrGroupService.getAttrsByAttrgroupId(attrgroupId);
        return R.ok().put("data", attrs);
    }

    /**
     * 查询未关联信息（以方便添加）
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNonRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrGroupService.getNonAttrsByAttrgroupId(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 删除关联
     */
    @PostMapping("attr/relation/delete")
    public R deleteRelation(@RequestBody RelationVo[] relationVos) {
        attrGroupService.batchDeleteAttrgroupAndAttrRelation(relationVos);
        return R.ok();
    }

    /**
     * 新增关联
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody RelationVo[] relationVos) {
        attrGroupService.batchAddAttrgroupAndAttrRelation(relationVos);
        return R.ok();
    }

    /**
     * 查询分类下所有分组&属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R RelationsAndAttrs(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupService.getAttrgroupsWithAttrsVos(catelogId);
        return R.ok().put("data", attrGroupWithAttrsVos);
    }
}
