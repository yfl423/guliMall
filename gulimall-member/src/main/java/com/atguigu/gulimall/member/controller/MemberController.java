package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.gulimall.member.exception.MobileNotOnlyException;
import com.atguigu.gulimall.member.exception.PasswordNotMatchUsernameException;
import com.atguigu.gulimall.member.exception.UsernameNotExistsException;
import com.atguigu.gulimall.member.exception.UsernameNotOnlyException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.atguigu.gulimall.member.vo.UserEnrollTo;
import com.atguigu.gulimall.member.vo.UserLoginTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 17:46:17
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R couponFeignTest() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();
        Object coupons = memberCoupons.get("coupons");
        R ok = R.ok();
        ok.put("member", memberEntity);
        ok.put("coupon", coupons);
        return ok;
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 保存注册会员
     */
    @PostMapping("/enroll")
    public R userEnroll(@RequestBody UserEnrollTo userEnrollTo) {
        try {
            memberService.enroll(userEnrollTo);
        } catch (UsernameNotOnlyException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        } catch (MobileNotOnlyException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 系统账户登录
     */
    @PostMapping("/login")
    public R loginAccount(@RequestBody UserLoginTo userLoginTo) {
        MemberEntity memberEntity;
        try {
            memberEntity = memberService.login(userLoginTo);

        } catch (UsernameNotExistsException e) {
            return R.error(BizCodeEnume.USERNAME_NOT_EXIST_EXCEPTION.getCode(), BizCodeEnume.USERNAME_NOT_EXIST_EXCEPTION.getMsg());
        } catch (PasswordNotMatchUsernameException e) {
            return R.error(BizCodeEnume.PASSWORD_NOT_MATCH_USERNAME_EXCEPTION.getCode(), BizCodeEnume.PASSWORD_NOT_MATCH_USERNAME_EXCEPTION.getMsg());
        }
        return R.ok().put("memberEntity", memberEntity);
    }

    /**
     * 社交登录
     */
    @PostMapping("/oauth/login")
    public R loginSocialAccount(@RequestBody SocialUser socialUser) {
        MemberEntity memberEntity = memberService.socialLogin(socialUser);
        return R.ok().put("memberEntity", memberEntity);
    }
}
