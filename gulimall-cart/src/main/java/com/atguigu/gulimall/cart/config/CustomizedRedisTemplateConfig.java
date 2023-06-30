package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class CustomizedRedisTemplateConfig {
    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisTemplate<String, Cart> CartRedisTemplate() {
        RedisTemplate<String, Cart> cartRedisTemplate = new RedisTemplate<>();
        cartRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return cartRedisTemplate;
    }
}
