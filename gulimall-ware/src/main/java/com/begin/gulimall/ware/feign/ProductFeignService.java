package com.begin.gulimall.ware.feign;

import com.begin.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     *
     * 1)、请求过网关
     * /api/product/skuinfo/info/{id}
     *  1、@FeignClient("gulimall-gateway"):给gulimall-gateway所在机器发送请求
     *
     * 2)、直接让后台指定服务处理
     * /product/skuinfo/info/{id}
     *  2、@FeignClient("gulimall-product")
     * @param id
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{id}")
    public R info(@PathVariable("id") Long id);
}
