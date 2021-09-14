package com.begin.gulimall.order.service.impl;

import com.begin.gulimall.order.entity.OrderEntity;
import com.begin.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.Query;

import com.begin.gulimall.order.dao.OrderItemDao;
import com.begin.gulimall.order.entity.OrderItemEntity;
import com.begin.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 声明需要监听的所有队列
     * <p>
     * 接收参数可以为以下类型
     * 1、Message message 原生消息详细信息。头+体
     * 2、T<发送的消息类型> OrderReturnReasonEntity
     * 3、Channel channel 当前传输数据的通道
     * <p>
     * Queue:可以很多人都拉监听消息。只要收到消息，队列删除消息，而且只能有一个收到消息
     * 场景：
     * 1）、订单服务启动多个:同一个消息，只能有一个客户端收到
     * 2)、只有一个消息完全处理完，方法运行结束。我们才可以接受下一个消息
     *
     * @param message
     */
    @RabbitHandler
    public void recieveMessage(Message message,
                               OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
        System.out.println("接收到消息==》内容" + content);
        message.getBody();//消息体信息
        message.getMessageProperties();//消息头属性信息
//        Thread.sleep(3000);
        System.out.println("消息处理完成=》" + content.getName());
        //channel内按顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (deliveryTag % 2 == 0) {
                //签收货物，非批量签收
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了货物..." + deliveryTag);
            } else {
                //拒收货物，两个方法效果一样，第一个多一个是否批量签收的参数
                //b true 批量拒收
                //b1 false丢弃。true重新入队
                channel.basicNack(deliveryTag, false, false);
//                channel.basicReject();
                System.out.println("没有签收货物");
            }

        } catch (IOException e) {
            //网路中断
            e.printStackTrace();
        }
    }

    @RabbitHandler
    public void recieveMessage2(
            OrderEntity content) throws InterruptedException {
        System.out.println("接收到消息==》内容" + content);
    }

}