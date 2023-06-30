package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.to.MemberTo;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignClient;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OAtuth2Controller {
    @Autowired
    MemberFeignClient memberFeignClient;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 使用code换取Access Token url: https://api.weibo.com/oauth2/access_token
        String host = "https://api.weibo.com";
        String path = "/oauth2/access_token";
        /**
         *
         必选	类型及范围	说明
         client_id	true	string	申请应用时分配的AppKey。
         client_secret	true	string	申请应用时分配的AppSecret。
         grant_type	true	string	请求的类型，填写authorization_code
         grant_type为authorization_code时
         必选	类型及范围	说明
         code	true	string	调用authorize获得的code值。
         redirect_uri	true	string	回调地址，需需与注册应用里的回调地址一致。
         */
        Map<String, String> map = new HashMap<>();
        // todo client_id
        map.put("client_id", "540748919");
        // todo client_secret
        map.put("client_secret", "bc9a01389f0ebff8f91f9890d9b05470");
        map.put("grant_type", "authorization_code");
        map.put("code", code);
        map.put("redirect_uri", "http://passport.gulimall.com/oauth2.0/weibo/success");
        HttpResponse response = HttpUtils.doPost(host, path, "post", new HashMap<>(), null, map);

        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取access token 成功，即已确定当前是哪个社交账户
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, new TypeReference<>() {
            });
            // 通过社交账户来登陆商城系统
            R r = memberFeignClient.loginSocialAccount(socialUser);
            if (r.getCode() == 0) {
                MemberTo memberTo = JSON.parseObject(JSON.toJSONString(r.get("memberEntity")), new TypeReference<>() {
                });
                // 登陆成功后跳转到商城首页
                session.setAttribute(AuthServerConstant.LOGIN_USER, memberTo);
                return "redirect:http://gulimall.com";
            } else {
                // 登陆失败
                return "redirect:http://passport.gulimall.com/login.html";
            }
        } else {
            // 获取access token 失败
            return "redirect:http://passport.gulimall.com/login.html";
        }
    }
}
