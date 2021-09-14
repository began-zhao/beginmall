package com.begin.gulimall.ware.vo;

import lombok.Data;

/**
 * 库存锁定返回结果
 */
@Data
public class LockStockResult {
    private Long skuId;//
    private Integer num;
    private Boolean locked;
}
