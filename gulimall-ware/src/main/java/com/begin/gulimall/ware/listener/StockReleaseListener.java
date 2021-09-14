package com.begin.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.begin.gulimall.common.to.mq.OrderTo;
import com.begin.gulimall.common.to.mq.StockDetailTo;
import com.begin.gulimall.common.to.mq.StockLockedTo;
import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.begin.gulimall.ware.entity.WareOrderTaskEntity;
import com.begin.gulimall.ware.service.WareSkuService;
import com.begin.gulimall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;


    /**
     * 监听死信队列，库存自动解锁
     * <p>
     * 只要解锁库存的消息失败，一定要告诉服务器解锁失败
     *
     * @param to
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息");
        try {
            Boolean redelivered = message.getMessageProperties().getRedelivered();//判断当前消息是否是第二次及以后派发过来的
            //解锁库存
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //重新回队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到订单关闭的消息，准备解锁库存");
        try {
            //解锁库存
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //重新回队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


}
