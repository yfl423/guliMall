package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.to.MemberTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignClient;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Autowired
    MemberFeignClient memberFeignClient;

    @PostMapping("/login")
    public String login(@Valid UserLoginVo userLoginVo, BindingResult result, RedirectAttributes redirectAttributes, HttpSession session) {
        if (result.hasErrors()) {
            // 校验失败
            Map<String, String> errorsMap = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errorsMap);
            errorsMap.put("userInfo", userLoginVo.getUserName()); // 用于回显
            return "redirect:http://passport.gulimall.com/login.html";
        } else {
            // 校验成功
            // 远程调用会员服务进行验证
            R r = memberFeignClient.loginAccount(userLoginVo);
            if (r.getCode() == 0) {
                // 登陆成功
                // todo
                MemberTo memberTo = JSON.parseObject(JSON.toJSONString(r.get("memberEntity")), new TypeReference<MemberTo>() {
                });
                // 登陆成功后跳转到商城首页
                session.setAttribute(AuthServerConstant.LOGIN_USER, memberTo);
                return "redirect:http://gulimall.com";
            } else {
                // 登录失败
                Map<String, Object> errorsMap = new HashMap();
                errorsMap.put("userInfo", userLoginVo.getUserName());
                Integer errorCode = r.getCode();
                if (errorCode == BizCodeEnume.USERNAME_NOT_EXIST_EXCEPTION.getCode()) {
                    errorsMap.put("userName", BizCodeEnume.USERNAME_NOT_EXIST_EXCEPTION.getMsg());
                } else if (errorCode == BizCodeEnume.PASSWORD_NOT_MATCH_USERNAME_EXCEPTION.getCode()) {
                    errorsMap.put("password", BizCodeEnume.PASSWORD_NOT_MATCH_USERNAME_EXCEPTION.getMsg());
                } else {
                    errorsMap.put("unknown", "系统未知异常");
                }
                redirectAttributes.addFlashAttribute("errors", errorsMap);
                return "redirect:http://passport.gulimall.com/login.html";
            }
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        if (session.getAttribute(AuthServerConstant.LOGIN_USER) != null){
            return "redirect:http://gulimall.com";
        }else {
            return "login";
        }
    }
}
