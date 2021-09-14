package com.begin.gulimall.order.config;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Configuration
public class MyRabbitConfig {

//    @Resource
    RabbitTemplate rabbitTemplate;
//    public MyRabbitConfig(RabbitTemplate rabbitTemplate){
//        this.rabbitTemplate=rabbitTemplate;
//        initRabbitTemplate();
//    }
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate=new RabbitTemplate(connectionFactory);
        this.rabbitTemplate=rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();;
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就回调
     *   1)、配置spring.rabbitmq.publisher-confirm
     *   2)、设置确认回调
     * 2、服务正确抵达队列进行回调
     * spring.rabbitmq.publisher-returns=true
     * spring.rabbitmq.template.mandatory=true
     *
     * 3、消费端确认（保证每个消息被正确消费，此时才可以broker删除这个消息）
     *      1、默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：收到很多消息，自动回复给服务器ack，只有一个消息处理成功。宕机了。发生消息丢失
     *      2、如何签收
     *          channel.basicAck(deliveryTag, false);签收
     *          channel.basicNack(deliveryTag, false, false);拒签
     */
//    @PostConstruct      //MyRabbitConfig对象创建完成后执行这个方法
    public void initRabbitTemplate() {
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *1、只要消息抵达Broker就b=true
             * @param correlationData 当前消息的唯一关联数据（）
             * @param b 消息是否成功收到
             * @param s 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                /**
                 * 做好消息确认机制
                 * 消费者手动ack
                 * 每一个发送的消息在数据库保存，定时将失败的重试
                 */
                //服务器收到消息
                System.out.println("confirm...correlationData[" + correlationData + "]==>[" + b + "]===>cause[" + s + "]");
            }
        });
        //设置消息抵达嘟列的确认回调
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param returnedMessage
             *
             *     private final Message message;   投递失败的详细信息
             *     private final int replyCode;     回复的状态码
             *     private final String replyText;  回复的文本内容
             *     private final String exchange;   当时这个消息发给那个交换机
             *     private final String routingKey; 当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                //报错误了，修改数据库当前消息的状态
                System.out.println("Fail Message[" + returnedMessage.getMessage() + "]==>replayCode[" + returnedMessage.getReplyCode() + "]" +
                        "==>replyText[" + returnedMessage.getReplyText() + "]" +
                        "==>exchange[" + returnedMessage.getExchange() + "]" +
                        "==>routingKey[" + returnedMessage.getRoutingKey() + "]");
            }
        });
    }
}
