package com.begin.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 17:47:51
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

