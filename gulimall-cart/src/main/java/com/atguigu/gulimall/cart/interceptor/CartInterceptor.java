package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.to.MemberTo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 所有访问购物车的请求都要先经过该拦截器
 * 该拦截器的作用用两个：1.确定用户信息，即根据是否携带session判断用户是否登陆； 2. 判断当前浏览器是否携带一个user-key cookie（关系到游客购物车）
 * 并将所有分析结果封装为userInfoTo,并放到threadLocal中，在整个线程共享
 */
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserInfoTo userInfoTo = new UserInfoTo();
        MemberTo memberTo = (MemberTo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberTo != null) {
            userInfoTo.setUserId(memberTo.getId());
        }
        UserInfoTo userInfo = setUserKey(request, response, userInfoTo);
        threadLocal.set(userInfo);
        return true;
    }


    private UserInfoTo setUserKey(HttpServletRequest request, HttpServletResponse response, UserInfoTo userInfoTo) {
        boolean hasCookie = false;
        if (request.getCookies() != null && request.getCookies().length > 0) {
            for (Cookie cookie : request.getCookies()) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    // 已经携带user-key，就直接封装
                    userInfoTo.setUserKey(cookie.getValue());
                    hasCookie = true;
                    break;
                }
            }
        }
        if (!hasCookie) {
            // 没有user-key就添加cookie
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, UUID.randomUUID().toString());
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_AGE);
            cookie.setDomain("gulimall.com");
            response.addCookie(cookie);
            userInfoTo.setUserKey(cookie.getValue());
        }
        return userInfoTo;
    }
}

