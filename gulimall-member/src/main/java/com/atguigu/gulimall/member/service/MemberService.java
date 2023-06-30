package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.vo.SocialUser;
import com.atguigu.gulimall.member.vo.UserEnrollTo;
import com.atguigu.gulimall.member.vo.UserLoginTo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 17:46:17
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 注册会员
     * @param userEnrollTo
     */
    void enroll(UserEnrollTo userEnrollTo);

    /**
     * 验证注册的会员名是唯一的
     */
    void checkUsername(String username);
    /**
     * 验证注册和手机号是唯一的
     */
    void checkMobile (String mobile);

    MemberEntity login(UserLoginTo userLoginTo);

    MemberEntity socialLogin(SocialUser socialUser);
}

