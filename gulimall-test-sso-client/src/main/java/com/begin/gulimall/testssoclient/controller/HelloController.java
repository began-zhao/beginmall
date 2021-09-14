package com.begin.gulimall.testssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {


    @Value("${sso.server.url}")
    String ssoServerUrl;

    /**
     * 无需登录就可以访问
     *
     * @return
     */
    @GetMapping("/hello")
    public String hello() {

        return "hello";
    }

    /**
     * 感知这次是在ssoserver登录成功跳回来的
     *
     * @param model
     * @param session
     * @param token  只要去ssoserver登录成功跳回来的标识
     * @return
     */
    @GetMapping("/employees")
    public String employees(Model model, HttpSession session,@RequestParam(value = "token",required = false) String token) {

        if(!StringUtils.isEmpty(token)){
            //TODO 1、去ssoserver获取当前token真正对应的用户信息
            session.setAttribute("loginUser","zhangsan");
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {

            //没登录，跳转到登录服务进行登录
            //使用URL的参数表示我们自己是那个页面
            return "redirect:"+ssoServerUrl+"?redirect_url=http://client1.com:8081/employees";
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("张三");
            emps.add("李四");
            model.addAttribute("emps", emps);
            return "list";

        }
    }
}
