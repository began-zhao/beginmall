package com.begin.gulimall.thirdparty.controller;

import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.thirdparty.compent.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone")String phone,@RequestParam("code") String  code){
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
