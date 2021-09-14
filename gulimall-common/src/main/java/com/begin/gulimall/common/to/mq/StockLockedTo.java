package com.begin.gulimall.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    private Long id;//库存工作单id
    private StockDetailTo detail;//工作单详情
}
