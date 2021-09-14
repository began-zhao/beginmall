package com.begin.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    /**
     * 订单分页查询
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pagNum",defaultValue = "1")Integer pageNum, Model model){

        //可获取到支付宝给我们创来的所有请求数据（需要验证签名)


        //查出当前登录的用户的所有订单列表数据
        Map<String,Object> page=new HashMap<>();
        page.put("page",pageNum.toString());
        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders",r);
        System.out.println(JSON.toJSONString(r));
        //查出当前登录的用户的所有订单列表数据
        return "orderList";
    }
}
