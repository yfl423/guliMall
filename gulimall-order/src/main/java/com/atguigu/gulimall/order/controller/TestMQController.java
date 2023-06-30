package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RabbitListener(queues = {"test_queue"})
@RestController
public class TestMQController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMQ")
    public String sendMQ(@RequestParam(defaultValue = "10") int times) {
        for (int i = 0; i < times; i++) {
            if (i % 2 == 0) {
                rabbitTemplate.convertAndSend("amp.direct", "test.queue", new OrderEntity());
            } else {
                rabbitTemplate.convertAndSend("amp.direct", "test.queue", new OrderItemEntity());
            }
        }
        return "ok";
    }

    @RabbitHandler
    public void receiveOrderMsg(OrderEntity orderEntity) {
        System.out.println("收到了orderEntity：" + orderEntity);
    }

    @RabbitHandler
    public void receiveOrderMsg(OrderItemEntity orderItemEntity) {
        System.out.println("收到了orderItemEntity：" + orderItemEntity);
    }
}
