package com.begin.gilimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.begin.gilimall.auth.feign.MemberFeignService;
import com.begin.gulimall.common.constant.AuthServerConstant;
import com.begin.gulimall.common.vo.MemberRespVo;
import com.begin.gilimall.auth.vo.SocialUser;
import com.begin.gulimall.common.utils.HttpUtils;
import com.begin.gulimall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理登录请求
 */
@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        //1、根据code换取token
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "1023378346");
        map.put("client_secret", "7f39b61591114cfb9da3539c5932dea7");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://gulimall.com:20000/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<String,String>(), null, map);

        //处理
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //知道是当前那个社交用户
            //1）、当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应用户信息）
            //登录或者注册社交用户
            R oauthlogin = memberFeignService.oauthlogin(socialUser);
            if (oauthlogin.getCode() == 0) {
                MemberRespVo data = oauthlogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登录成功：用户{}", data.toString());
                //TODO 1、默认发的令牌。作用域为当前作用域（解决子域session共享问题）
                //TODO 2、使用JSON序列化方式来序列化对象数据到redis中
                session.setAttribute(AuthServerConstant.LOGIN_USER,data);
                //2、登录成功就调回首页
//                return "redirect:http://localhost:10000";
                return "redirect:http://gulimall.com:10000/";

            } else {
                //失败重定向到登录页
                return "redirect:http://gulimall.com:20000/login.html";
            }


        } else {
            //失败重定向到登录页
            return "redirect:http://gulimall.com:20000/login.html";
        }

    }
}
