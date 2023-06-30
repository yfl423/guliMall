package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.atguigu.gulimall.product.vo.AttrVoResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 23:22:52
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("{attrType}/list/{catelogId}")
    public R baseAttrlist(@RequestParam Map<String, Object> params, @PathVariable("attrType") String type, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrService.queryBasePage(params, type, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);
        return R.ok().put("data", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrVoResponse attr = attrService.getDetailsById(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updatAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

    /**
     * 规格维护： 获取对应属性列表
     * /product/attr/base/listforspu/{spuId}
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R getBaseAttr(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.getBaseMapper().selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return R.ok().put("data", productAttrValueEntities);
    }

    /**
     * 规格维护：修改商品规格
     */
    @PostMapping("/update/{spuId}")
    public R updateBaseAttr(@RequestBody List<ProductAttrValueEntity> attrs, @PathVariable("spuId") Long spuId) {
        productAttrValueService.updateBaseAttr(attrs,spuId);
        return R.ok();
    }

}
