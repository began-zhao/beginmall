package com.begin.gulimall.order.config;

import com.begin.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {





    /**
     * 容器中的Binding.Queue,Exchange都会自动创建
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        Map<String,Object> arguments=new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        Queue queue = new Queue("order.delay.queue", true, false, false,arguments);
        return  queue;

    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return  queue;
    }
    @Bean
    public Exchange orderEventExchange(){
        TopicExchange exchange = new TopicExchange("order-event-exchange", true, false);
        return exchange;
    }
    @Bean
    public Binding orderCreateOrderBinding(){
        Binding binding = new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",null);
        return  binding;
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        Binding binding = new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",null);
        return  binding;
    }

    /**
     * 订单释放直接和库存释放绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        Binding binding = new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",null);
        return  binding;
    }

    /**
     * 秒杀下单队列（流量削峰）
     * @return
     */
    @Bean
    public Queue orderSeckillOrderQueue(){
        Queue queue = new Queue("order.seckill.order.queue", true, false, false);
        return  queue;
    }
    @Bean
    public Binding orderSeckillOrderBinding(){
        Binding binding = new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",null);
        return  binding;
    }
}
