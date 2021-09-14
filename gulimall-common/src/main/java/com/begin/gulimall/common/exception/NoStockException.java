package com.begin.gulimall.common.exception;

public class NoStockException extends RuntimeException {
    private Long skuId;
    public NoStockException(Long skuId){
        super("商品id:"+skuId+"；没有足够的库存了");
    }
    public NoStockException(String msg){
        super("库存锁定失败:"+msg);
    }
    public NoStockException(){
        super("库存锁定失败");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
