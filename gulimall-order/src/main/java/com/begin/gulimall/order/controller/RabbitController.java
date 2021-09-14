package com.begin.gulimall.order.controller;


import com.begin.gulimall.order.entity.OrderEntity;
import com.begin.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@RestController
@Slf4j
public class RabbitController {

    @Resource
    RabbitTemplate rabbitTemplate;


    @GetMapping("/sendMQ")
    public String sendMQ(@RequestParam(value = "num",defaultValue = "10") Integer num){
        for (int i = 0; i < 10; i++) {
            if (i%2==0) {
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setId(1L);
                entity.setCreateTime(new Date());
                entity.setName("hahaha" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity,new CorrelationData());
            }else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity,new CorrelationData());
            }
        }
        return  "ok";
    }
}
