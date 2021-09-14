package com.begin.gulimall.order.to;

import com.begin.gulimall.order.entity.OrderEntity;
import com.begin.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单的信息
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;//订单对应计算价格
    private BigDecimal fare;//运费
}
