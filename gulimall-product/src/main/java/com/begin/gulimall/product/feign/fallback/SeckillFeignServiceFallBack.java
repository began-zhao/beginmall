package com.begin.gulimall.product.feign.fallback;

import com.begin.gulimall.common.exception.BizCodeEnume;
import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.product.feign.SeckillFeignServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignServer {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断方法调用...");
        return R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(), BizCodeEnume.TOO_MANY_REQUEST.getMessage());
    }
}
