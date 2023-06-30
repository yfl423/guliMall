package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.enume.OrderStatusEnum;
import com.atguigu.common.to.mq.OrderEntityTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderEntityVo;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            // 这只是一个冗余信息，如果因为远程调用过程出了异常，而使的整个数据回滚没必要
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常，异常不往上抛了，事务也就不回滚了
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }


            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId(skuId);
            Long stock = wareSkuDao.calStocksBySkuId(skuId);
            // 如果stock不存在，返回为null，会发生空指针异常，所以要提前判断
            vo.setHasStock(stock == null ? false : stock > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    @Transactional
    public boolean lockStock(WareSkuLockVo vo) throws NoStockException {
        //  保存库存工作单的详情
        // 用于回滚的追溯
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.saveAndGeneratedKey(wareOrderTaskEntity);
        for (OrderItemVo item : vo.getItems()) {
            boolean skuStock = false;
            Long skuId = item.getSkuId();
            Integer skuQuantity = item.getSkuQuantity();
            List<WareSkuEntity> stockDistribution = baseMapper.getStockDistribution(skuId);
            if (stockDistribution != null && stockDistribution.size() > 0) {
                for (WareSkuEntity wareSkuEntity : stockDistribution) {
                    Long count = baseMapper.updateStockLock(skuId, wareSkuEntity.getWareId(), skuQuantity);
                    if (count == 1) {
                        skuStock = true;
                        // 保存每一个订单项的库存工作单详情：包括skuId，在哪个仓库锁定，锁定多少，属于哪个库存工作单（唯一订单号），锁定状态
                        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, item.getSkuName(), skuQuantity, wareOrderTaskEntity.getId(), wareSkuEntity.getWareId(), 1);
                        wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                        StockLockedTo stockLockedTo = new StockLockedTo(wareOrderTaskEntity.getId(), wareOrderTaskDetailEntity.getId());
                        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                        break;
                    }
                }
                if (skuStock == false) {
                    throw new NoStockException(skuId);
                }
            } else {
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Override
    public void unlockStockService(StockLockedTo stockLockedTo) {
        Long detailId = stockLockedTo.getDetailId();
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        // 一种可能的情况是：库存不够导致锁库存失败，那么整个事务中包括保存库存任务，详情以及所有锁库存操作全部回滚，但是回滚前成功锁定的订单项的消息已经被发送
        // 这就意味着我们的消息队列中可能存在着一些冗余消息，即消息中的任务id和任务详情id在数据库中根本不存在
        // 对于这种情况，我们只需要判断一下即可，因为这种情况下锁库存的操作也回滚了，所以无需自动解锁
        if (byId != null) {
            // 解锁逻辑: 能得到库存工作单，说明锁定库存成功，但我们仍需要做一系列判断来确定是否解锁库存
            // 1. 利用库存工作单的订单号去查订单，如果能查不到，说明下订单的过程后面存在异常（比如扣减积分出错），整个事务回滚，根本没产生订单
            // 这种情况必须解锁库存！
            // 2. 能查到订单，但发现订单的状态是取消，这说明整个下订单过程正常完成，不过在订单的有效期内用户手动取消了订单，这种情况也要解锁库存！
            Long id = stockLockedTo.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderByOrderSn(orderSn);
            if (r.getCode() == 0) {
                OrderEntityVo order = JSON.parseObject(JSON.toJSONString(r.get("data")), OrderEntityVo.class);
                if (order == null || order.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
                    // 订单不存在 即情况一
                    // 订单已被用户取消，情况二
                    // 解锁
                    if (byId.getLockStatus() == 1) { // 只要当前库存工作单详情是1，代表未解锁，我们才能解锁，否则我们就不需要解锁
                        // （因为锁库存引发的解锁库存逻辑不是唯一的解锁逻辑，我们还要订单系统引发的主动解锁逻辑，这里的判断是避免重复解锁）
                        Integer skuNum = byId.getSkuNum();
                        Long skuId = byId.getSkuId();
                        Long wareId = byId.getWareId();
                        unlockStock(skuId, skuNum, wareId, detailId);
                    }
                }  // 如果订单存在且没有被取消，无需解锁库存
            } else {
                throw new RuntimeException("远程服务失败");
            }
        } // 如果订单不存在，说明是冗余消息，也无需解锁库存
    }

    @Override
    public void unlockStockService(OrderEntityTo orderEntityTo) {
        String orderSn = orderEntityTo.getOrderSn();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        Long taskId = taskEntity.getId();
        List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.getBaseMapper().selectList(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", taskId)
                        .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
            Long wareId = detailEntity.getWareId();
            Long skuId = detailEntity.getSkuId();
            Integer skuNum = detailEntity.getSkuNum();
            Long detailId = detailEntity.getId();
            unlockStock(skuId, skuNum, wareId, detailId);
        }
    }

    /**
     * 在数据库中解锁库存
     *
     * @param skuId
     * @param skuNum
     * @param wareId
     * @param detailId
     */
    @Transactional
    void unlockStock(Long skuId, Integer skuNum, Long wareId, Long detailId) {
        wareSkuDao.unlockStock(skuId, wareId, skuNum);
        // 解锁库存后，也要更新库存工作单的状态 1：已锁定 2：解锁成功 3：已扣除
        wareOrderTaskDetailService.update(new UpdateWrapper<WareOrderTaskDetailEntity>().set("lock_status", 2).eq("id", detailId));
    }

//    @Transactional
//    void lockStock(OrderEntityTo orderEntityTo, List<OrderItemVo> vos) {
//
//        for (OrderItemVo vo : vos) {
//            int count = getCount(vo.getSkuId()); // select count(stock - stock_locked) from ware_sku where skuId = id;
//            if (count < vo.getSkuQuantity()) throw new NoStockException(vo.getSkuId()); // 所有仓库库存不够订单的个数要求，直接回滚
//        }
//        String addr = orderEntityTo.getReceiverDetailAddress();
//        Integer[] wareIds = sortByDist(addr); // 根据收货地址和仓库间距离对仓库排序
//        for (OrderItemVo vo : vos) {
//            int i = 0;
//            long skuId = vo.getSkuId();
//            int q = vo.getSkuQuantity(); // 该sku需要的总数量
//            while (q > 0 && i < wareIds.length) {
//                int stock = getStock(skuId, wareIds[i]); // 查询该仓库的该sku库存数
//                if (stock > q) {
//                    lockStock(skuId, wareIds[i], q);
//                    q = 0;
//                } else {
//                    lockStock(skuId, wareIds[i], stock);
//                    q -= stock;
//                }
//                i++;
//            }
//            if (q > 0) throw new NoStockException(skuId);
//        }
//    }
}