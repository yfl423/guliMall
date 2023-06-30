package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignClient;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignClient;
import com.atguigu.gulimall.auth.vo.UserEnrollVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class EnrollController {
    @Autowired
    ThirdPartyFeignClient thirdPartyFeignClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignClient memberFeignClient;

    @GetMapping("/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        // 接口防刷
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;
        String redisCode = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(redisCode)) {
            // 不为空才做频率的校验，为空说明第一次发验证码，无需校验发送频率
            long l = Long.parseLong(redisCode.split("_")[1]);
            if ((System.currentTimeMillis() - l) < 60000) {
                // 频率过高
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5); // 短信发送的5位验证码
        String codeInRedis = code + "_" + System.currentTimeMillis(); // 保存在数据库中加了时间戳的数据
        // 远程调用第三方服务发送短信验证码
//        R r = thirdPartyFeignClient.sendCode(phone, code);
        // todo 测试状态
        R r =R.ok();
        r.put("ttl",10);
        System.out.println(code);
        if (r != null && r.getCode() == 0) {
            // 成功发送验证码
            int ttl = (int) r.get("ttl");
            redisTemplate.opsForValue().set(key, codeInRedis, (long) ttl, TimeUnit.MINUTES);
        }
        return r;
    }

    @PostMapping("/enroll")
    public String enroll(@Valid UserEnrollVo userEnrollVo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {

            Map<String, Object> errorsMap = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
            errorsMap.put("userInfo",userEnrollVo.getUserName());
            errorsMap.put("phoneInfo",userEnrollVo.getPhone());
            redirectAttributes.addFlashAttribute("errors", errorsMap);
            return "redirect:http://passport.gulimall.com/enroll.html";
        }
        // 经过JSR303的校验后所有的数据一定非空且合法
        String code = userEnrollVo.getCode();
        String phone = userEnrollVo.getPhone();
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;
        String s = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(s)) {
            if (code.equalsIgnoreCase(s.split("_")[0])) {
                // 验证码验证成功 ，令牌机制，一旦验证成功后，删除验证码使之失效，防止重复验证
                redisTemplate.delete(key);
                // 调用远程服务完成注册过程
                R r = memberFeignClient.userEnroll(userEnrollVo);
                if (r.getCode() == 0) {
                    // 远程调用成功，注册成功，跳转到登录页面
                    return "redirect:http://passport.gulimall.com/login.html";
                } else {
                    // 注册失败
                    Map<String, Object> errorsMap = new HashMap();
                    errorsMap.put("userInfo",userEnrollVo.getUserName());
                    errorsMap.put("phoneInfo",userEnrollVo.getPhone());
                    Integer errorCode = r.getCode();
                    if (errorCode == BizCodeEnume.USER_EXIST_EXCEPTION.getCode()) {
                        errorsMap.put("userName", BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
                    } else if (errorCode == BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode()) {
                        errorsMap.put("phone", BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
                    } else {
                        errorsMap.put("unknown", "系统未知异常");
                    }

                    redirectAttributes.addFlashAttribute("errors", errorsMap);
                    return "redirect:http://passport.gulimall.com/enroll.html";
                }
            } else {
                // 验证码错误
                Map<String, Object> errorsMap = new HashMap();
                errorsMap.put("userInfo",userEnrollVo.getUserName());
                errorsMap.put("phoneInfo",userEnrollVo.getPhone());
                errorsMap.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errorsMap);
                return "redirect:http://passport.gulimall.com/enroll.html";
            }
        } else {
            // 验证码失效
            Map<String, Object> errorsMap = new HashMap();
            errorsMap.put("userInfo",userEnrollVo.getUserName());
            errorsMap.put("phoneInfo",userEnrollVo.getPhone());
            errorsMap.put("code", "验证码已过期");
            redirectAttributes.addFlashAttribute("errors", errorsMap);
            return "redirect:http://passport.gulimall.com/enroll.html";
        }
    }

    @GetMapping("/enroll.html")
    public String loginPage(HttpSession session){
        if (session.getAttribute("loginUser") != null){
            return "redirect:http://gulimall.com";
        }else {
            return "enroll";
        }
    }
}
