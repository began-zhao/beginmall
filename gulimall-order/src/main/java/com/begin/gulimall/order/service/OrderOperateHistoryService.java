package com.begin.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 17:21:37
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

