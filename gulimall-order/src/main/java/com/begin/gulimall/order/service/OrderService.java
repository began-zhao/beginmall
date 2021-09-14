package com.begin.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.to.mq.SeckillOrderTo;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.order.entity.OrderEntity;
import com.begin.gulimall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 17:21:36
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 给订单确认页返回需要的数据
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单方法
     * @param vo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    /**
     * 远程返回订单状态
     * @param orderSn
     * @return
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 关闭过期订单
     * @param entity
     */
    void closeOrder(OrderEntity entity);

    /**
     * 获取当前订单信息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    /**
     * 处理支付宝支付返回数据
     * @param vo
     * @return
     */
    String handlePayResult(PayAsyncVo vo);

    /**
     * 准备创建秒杀单的详细信息
     * @param entity
     */
    void createSeckillOrder(SeckillOrderTo entity);
}

