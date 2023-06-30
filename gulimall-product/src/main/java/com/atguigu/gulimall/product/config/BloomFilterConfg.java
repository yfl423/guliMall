package com.atguigu.gulimall.product.config;

import com.google.common.hash.BloomFilter;
import org.redisson.RedissonBloomFilter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfg {
    @Autowired
    RedissonClient redissonClient;

    static final String SKU_BF_PREFIX = "gulimall:product:skuBF";

    @Bean
    public RBloomFilter<Long> bloomFilter() {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(SKU_BF_PREFIX);
        bloomFilter.tryInit(100000, 0.0001);
        return bloomFilter;
    }
}
