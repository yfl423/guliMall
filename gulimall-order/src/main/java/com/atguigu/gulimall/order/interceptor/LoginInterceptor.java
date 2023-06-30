package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.to.MemberTo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        // 如果是自动解锁的远程调用请求，就直接放行
        boolean match = new AntPathMatcher().match("/order/order/status/**", requestURI);
        if (match) {
            return true;
        }
        HttpSession session = request.getSession();
        MemberTo loginUser = (MemberTo) session.getAttribute("loginUser");
        if (loginUser != null) {
            threadLocal.set(loginUser);
            return true;
        }
        session.setAttribute("msg", "请先登录!");
        response.sendRedirect("http://passport.gulimall.com/login.html");
        return false;
    }
}
