package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers (ViewControllerRegistry registry){
        // 相当于路径不经controller直接和视图映射
        // 我们希望如果已经登陆的用户即便访问登陆页也应该直接跳回到首页，那么这种直接映射的方式就不能满足这个业务需求了
        // 因为这里不能自定义逻辑，只能直接映射
//        registry.addViewController("/login.html").setViewName("login");
//        registry.addViewController("/enroll.html").setViewName("enroll");
    }
}
