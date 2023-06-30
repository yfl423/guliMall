package com.atguigu.gulimall.cart;

import com.atguigu.gulimall.cart.vo.Cart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallCartApplicationTests {
    @Autowired
    RedisTemplate<String, Cart> cartRedisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Test
    public void contextLoads() {

    }

}
