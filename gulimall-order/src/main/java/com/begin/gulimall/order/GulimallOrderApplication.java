package com.begin.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1、引入amqp场景;RabbitAutoConfiguration
 * 2、给容器中配置了
 *      rabbitTemplate、amqpAdmin、rabbitMessagingTemplate、rabbitConnectionFactory
 * 3、@EnableRabbit:开启功能
 * 5、监听消息：使用@RabbitListeners：必须开启EnableRabbit
 * @RabbitListeners ：类或方法上
 * @RabbitHandler ：标在方法上
 *
 * 本地事务失效问题
 * 同一个对象内事务方法糊掉默认失效，原因 绕过了代理对象，事务使用代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 * 1)、引入spring-boot-starter-aop；使用其中的aspectj
 * 2）、@EnableAspectJAutoProxy，开启aspectj动态代理功能，以后所有的动态代理功能都是aspectj生成的（计时没有接口也可以创建动态代理）
 *      exposeProxy = true对外暴露代理对象
 * 3）、使用代理对象本类互调
 *      获取代理对象使用OrderServiceImpl orderService=(OrderServiceImpl) AopContext.currentProxy();
 *      orderService.b();
 *      orderService.c();
 *
 *  Seata控制分布式事务
 *  1）、每一个微服务数据库线必须创建undo_log表
 *  2）、安装事务协调器：seata-server: https://github.com/seata/seata/releases
 *  3)、整合
 *      1、导入依赖spring-cloud-starter-alibaba-seata seata-all-1.1.0
 *      2、启动seata服务器
 *          registry.conf 注册中心相关的配置 ：修改registry type=nacos
 *      3、标注注解使用@GlobalTransactional,所有想要用到的分布式事务的微服务用seata DataSourceProxy代理自己的数据源
 *      4、每个微服务都必须导入register.config和file.config
 *          file.config:vgroupMapping.{application-name}fescar-service-group = "default"
 *      5、给分布式大事务的入口标注@GlobalTransactional，远程小事务使用@Transactional
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
