package com.begin.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 17:47:51
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn);
}

