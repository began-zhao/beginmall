package com.begin.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 1、设置配置中心
 *    1）、引入依赖，
 *    2）、创建一个bootstrap.properties.
 *    3)、给配置中心默认添加一个数据集
 *    4）、给应用名添加自定义配置
 *    5）、动态获取配置
 *          @RefreashScope:动态刷新配置
 *          @value获取到配置
 *          如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用配置中心的项
 *2、细节
 * 1）、命名空间：配置隔离：
 *      默认:public(保留空间)；默认新增的所有配置都在public空间。
 *      1、开发，测试，生产：利用命名空间来做环境隔离。
 *      注意：需要使用那个命名空间的配置需要在bootstrap.properties下指定spring.cloud.nacos.config.namespace=c44bb4a4-7eda-419c-bf94-ca9ac09f66fc
 *      2、每一个微服务之间相互隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
 *
 *  2）、配置集：所有配置的集合
 *
 *  3）、配置集ID：类似文件名。
 *      DataID：类似文件名
 *
 *  4）、配置分组：
 *      默认所有的配置集都属于：DEFAULT_GROUP;
 *
 * 每个微服务创建自己的命名空间，使用配置分组区分环境，dev、test、prod
 *
 * 3)、同时加载多个配置集
 *  1）、微服务任何配置信息，任何配置文件都可以放在配置中心中
 *  2）、只需要早bootstrap.properties...
 *  3)、@Value，@ConfigurationProperties...
 *  以前SpringBoot任何方法从配置文件中获取值，都能使用。
 *  配置中心有的优先使用配置中心的
 *
 *
 *
 * */
@EnableTransactionManagement
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
