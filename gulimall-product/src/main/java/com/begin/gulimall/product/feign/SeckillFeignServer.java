package com.begin.gulimall.product.feign;

import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.product.feign.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "gulimall-seckill")//,fallback = SeckillFeignServiceFallBack.class
public interface SeckillFeignServer {
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
