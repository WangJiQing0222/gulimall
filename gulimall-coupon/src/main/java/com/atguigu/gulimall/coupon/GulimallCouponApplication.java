package com.atguigu.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 配置管理
 * 1、引依赖
 *         <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
 *         </dependency>
 *  2、创建bootstrap.properties配置文件，添加属性值，使用@Value("${属性}")获取值
 *  3、nacos动态获取配置
 *          nacos控制台添加配置，Data ID为gulimall-coupon.properties
 *          在使用该类上添加@RefreshScope，动态获取nacos配置的值
 *          配置中心的优先级大于bootstrap.properties，属性值会覆盖bootstrap.properties中的值
 *
 *  4、命名空间和配置分组
 *      每个微服务有自己的命名空间，在bootstrap.properties中配置
 *      一个命名空间下可以创建多个分组，比如，命名空间coupon下有dev、prop分组
 */

@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
