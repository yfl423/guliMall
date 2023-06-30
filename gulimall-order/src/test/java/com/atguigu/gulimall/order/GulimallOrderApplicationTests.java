package com.atguigu.gulimall.order;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    RabbitAdmin rabbitAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void contextLoads() {
//        rabbitAdmin.declareBinding(new Binding());
//        rabbitAdmin.declareExchange(new DirectExchange("java_exchange"));
//        OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
//        orderReturnReasonEntity.setCreateTime(new Date());
//        orderReturnReasonEntity.setId(1L);
//        orderReturnReasonEntity.setName("gulimall");
//        orderReturnReasonEntity.setSort(1);
//        orderReturnReasonEntity.setStatus(1);
//
////        String s = JSON.toJSONString(orderReturnReasonEntity);
//
//        rabbitTemplate.convertAndSend("gulimall_dir","gulimall",orderReturnReasonEntity);
    }

    @Test
    public void testThreadLocal() {

    }
}
