package com.begin.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 *1、整合Sentinel
 *  1）、导入依赖spring-cloud-starter-alibaba-sentinel
 *  2)、下载sentinel对应版本控制台
 *  3)、配置sentinel控制台地址
 *  4）、在控制台调整所有参数。【默认所有的流控设置保存在内存中，重启失效】
 *
 * 2、每一个微服务都导入actuator并配合management.endpoints.web.exposure.include=*
 * 3、自定义sentinelConfig
 */
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
