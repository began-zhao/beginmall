package com.begin.gulimall.order;

import com.begin.gulimall.order.entity.OrderEntity;
import com.begin.gulimall.order.entity.OrderReturnApplyEntity;
import com.begin.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;


@SpringBootTest
@Slf4j
class GulimallOrderApplicationTests {

    /**
     * 创建配置
     */
    @Resource
    AmqpAdmin amqpAdmin;

    /**
     * 接受发消息
     */
    @Resource
    RabbitTemplate rabbitTemplate;


    @Test
    public void sendMessageTest() {


        //1、发送消息，如果发送的消息是对象，会使用序列化机制，将对象写出去，对象必须实现Serializable
        String msg = "Hello World!";

        //2、发送的对象消息，可以是一个json
        //发送对象
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setId(1L);
                entity.setCreateTime(new Date());
                entity.setName("hahaha" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity, new CorrelationData());
                log.info("消息发送成功：{}", msg);
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity, new CorrelationData());
                log.info("消息发送成功：{}", msg);
            }
        }

    }


    /**
     * 1、如何创建Exchange、Queue、Binding
     * 1）、使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {

        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    @Test
    public void createQueue() {

        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    @Test
    public void createBinding() {
        /*
         * String destination,【目的地】
         *  Binding.DestinationType destinationType,【目的地类型】
         *  String exchange,【交换机】
         *  String routingKey,【路由键】
         *  @Nullable Map<String, Object> arguments【自定义参数】
         * 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
         * */
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("binding[{}]创建成功", "hello-java-bind");
    }

}
