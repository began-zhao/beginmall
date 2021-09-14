package com.begin.gulimall.seckill.scheduled;

import com.begin.gulimall.seckill.scheduled.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品定时上架
 *  每天晚上3点；上架最近三天需要秒杀的商品
 *
 */
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    //分布式锁
    private final String upload_lock="seckill:upload:lock";

    //TODO 幂等性处理
    @Scheduled(cron = "*/3 * * * * ?")
    public void uploadSeckillSkuLatest3Days(){
        //1、重复上架无需处理
        log.info("上架秒杀的商品信息...");
        // 分布式锁
        RLock lock = redissonClient.getLock(upload_lock);

        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }

    }

}
