package com.atguigu.gulimall.member.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // /order/order/status/{orderSn} 根据订单号获取订单无需拦截，库存服务解锁时需要使用
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", uri);
        if (match) {
            return true;
        }


        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null) {
            //登录
            loginUser.set(attribute);
            return true;//放行
        }else {
            //没登陆
            request.getSession().setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;//拦截
        }
    }
}
