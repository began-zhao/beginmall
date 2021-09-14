package com.begin.gulimall.order.listener;

import com.begin.gulimall.common.to.mq.SeckillOrderTo;
import com.begin.gulimall.order.entity.OrderEntity;
import com.begin.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "order.seckill.order.queue")
@Component
@Slf4j
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;
    /**
     * 监听秒杀单信息
     * @param entity
     */
    @RabbitHandler
    public void listener(SeckillOrderTo entity, Channel channel, Message message) throws IOException {
        try {
            log.info("准备创建秒杀单的详细信息。。。");
            orderService.createSeckillOrder(entity);
            //手动调支付宝收单
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
