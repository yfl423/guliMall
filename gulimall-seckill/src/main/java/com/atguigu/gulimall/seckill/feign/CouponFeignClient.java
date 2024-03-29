package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-coupon")
public interface CouponFeignClient {
    @GetMapping("coupon/seckillsession/get3LatestDaysSession")
    R get3LatestDaysSession(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime);
}
