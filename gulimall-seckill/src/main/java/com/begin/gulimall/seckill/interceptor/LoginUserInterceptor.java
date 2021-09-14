package com.begin.gulimall.seckill.interceptor;

import com.begin.gulimall.common.constant.AuthServerConstant;
import com.begin.gulimall.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> LoginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        ///order/order/status/{orderSn}
        String uri = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean match = matcher.match("/kill", uri);
        if (match) {
            MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);

            if (attribute != null) {
                //已登录
                LoginUser.set(attribute);
                return true;
            } else {
                //未登录
                request.getSession().setAttribute("msg", "请先登录");
                response.sendRedirect("http://auth.guilimall.com:20000/login.html");
                return false;
            }
        }
        return true;
    }
}
