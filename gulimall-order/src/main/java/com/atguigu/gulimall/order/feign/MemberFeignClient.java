package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignClient {
    @GetMapping("member/memberreceiveaddress/{memberId}/addressInfo")
    List<MemberAddressVo> getAddressInfo(@PathVariable("memberId") Long memberId);
}
