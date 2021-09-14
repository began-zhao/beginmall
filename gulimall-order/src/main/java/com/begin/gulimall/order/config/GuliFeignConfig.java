package com.begin.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            /**
             *
             * @param requestTemplate 新请求
             */
            @Override
            public void apply(RequestTemplate requestTemplate) {

                //使用RequestContextHolder获取进来的请求数据
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();//老请求
                    if (request != null) {
                        //同步请求数据，cookie
                        String cookies = request.getHeader("Cookie");
                        //给新请求同步了老请求的Cookie
                        requestTemplate.header("Cookie", cookies);
                    }
                }
            }
        };
    }
}
