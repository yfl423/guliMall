package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.mq.OrderEntityTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.vo.OrderEntityVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockRespVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:59:40
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);


    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * @return
     */
    boolean lockStock(WareSkuLockVo vo) throws NoStockException;

    /**
     * 被动引发的自动解锁逻辑（由于库存锁定）
     * @param stockLockedTo
     */
    void unlockStockService(StockLockedTo stockLockedTo);

    /**
     * 主动引发的自动解锁逻辑（由于订单关闭）
     * @param orderEntityTo
     */
    void unlockStockService(OrderEntityTo orderEntityTo);
}

