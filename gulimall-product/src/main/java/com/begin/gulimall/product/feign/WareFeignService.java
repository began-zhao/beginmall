package com.begin.gulimall.product.feign;

import com.begin.gulimall.common.to.SkuHasStockVo;
import com.begin.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
     R getSkusHasStock(@RequestBody List<Long> skuIds);
}
