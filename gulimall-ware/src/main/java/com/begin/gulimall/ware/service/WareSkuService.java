package com.begin.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.to.mq.OrderTo;
import com.begin.gulimall.common.to.mq.StockLockedTo;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.ware.entity.WareSkuEntity;
import com.begin.gulimall.ware.vo.LockStockResult;
import com.begin.gulimall.ware.vo.SkuHasStockVo;
import com.begin.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 17:47:51
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo to);
}

