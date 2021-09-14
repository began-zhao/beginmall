package com.begin.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.begin.gulimall.order.config.AlipayTemplate;
import com.begin.gulimall.order.service.OrderService;
import com.begin.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * 1、将支付页让浏览器展示
     * 2、支付成功后，我们要调到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @GetMapping(value = "/payOrder",produces = "text/html")
    @ResponseBody
    @CrossOrigin
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        //获取当前订单支付信息
        PayVo payVo= orderService.getOrderPay(orderSn);
        //返回的是一个页面，将此页面直接返回给浏览器，会出现扫码支付页面
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }
}
