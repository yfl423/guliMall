package com.atguigu.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockRespVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 商品库存
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:59:40
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 查询是否有库存
     *
     * @param skuIds
     * @return
     */
    @PostMapping("/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockVo> stocInfo = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().put("data", stocInfo);
    }

    /**
     * 保存订单后锁定库存
     */
    @PostMapping("/order/lock")
    public R lockStockAfterOrder(@RequestBody WareSkuLockVo vo) {
        try {
            boolean res = wareSkuService.lockStock(vo);
            return R.ok().put("data",res);
        } catch (NoStockException e) {
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }
}
