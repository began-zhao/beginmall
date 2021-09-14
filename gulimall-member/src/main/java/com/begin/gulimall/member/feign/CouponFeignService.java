package com.begin.gulimall.member.feign;

import com.begin.gulimall.common.utils.R;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 83456
 * 这是一个声明式的远程调用
 */
@FeignClient(value = "gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("coupon/coupon/member/list")
    R membercoupons();
}
