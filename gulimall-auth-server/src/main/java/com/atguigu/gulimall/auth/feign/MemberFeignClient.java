package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserEnrollVo;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignClient {
    @PostMapping("member/member/enroll")
    R userEnroll(@RequestBody UserEnrollVo userEnrollVo);

    @PostMapping("member/member/login")
    R loginAccount(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("member/member/oauth/login")
    R loginSocialAccount(@RequestBody SocialUser socialUser);
}
