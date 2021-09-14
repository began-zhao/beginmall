package com.begin.gulimall.order.vo;

import com.begin.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity orderEntity;
    private Integer code;//0成功 ，错误状态码
}
