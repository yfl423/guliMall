package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.OrderEntityTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderEntityVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;

    /**
     * 接收由于库存锁定产生的被动解锁库存逻辑
     *
     * @param message
     * @param stockLockedTo
     */
    @RabbitHandler
    public void handleStockLockedRelease(Message message, StockLockedTo stockLockedTo, Channel channel) throws IOException {
        System.out.println("收到由于库存锁定产生的解锁库存的消息");
        try {
            wareSkuService.unlockStockService(stockLockedTo);
            // 只要没有异常，不管是我们根据消息解锁了库存，或者是订单不存在或者已支付等无需解锁的情况。。。都说明消息已被消费，应回复确收
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 任何异常（比如远程调用失败等）都说明解锁库存失败，我们应该拒收消息，并把消息加回队列，让别的机器来解决
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 接收由于订单关闭产生的主动解锁库存逻辑
     *
     * @param message
     * @param orderEntityTo
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleOrderClosed(Message message, OrderEntityTo orderEntityTo, Channel channel) throws IOException {
        System.out.println("收到由于订单关闭产生的解锁库存的消息");
        try {
            wareSkuService.unlockStockService(orderEntityTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 任何异常（比如远程调用失败等）都说明解锁库存失败，我们应该拒收消息，并把消息加回队列，让别的机器来解决
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
