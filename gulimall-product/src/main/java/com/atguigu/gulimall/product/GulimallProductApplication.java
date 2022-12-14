package com.atguigu.gulimall.product;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosDiscoveryAutoConfiguration;
import com.alibaba.cloud.nacos.client.NacosPropertySourceLocator;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 整合Mybatis-Plus
 *      1)、导入依赖
 *         <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *         </dependency>
 *      2)、配置
 *          1、配置数据源
 *
 *          2、配置Mybatis-Plus：
 *              1)、使用@MapperScan
 *              2)、告诉Mybatis-Plus，sql映射文件位置
 */
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication(exclude = {NacosDiscoveryAutoConfiguration.class})
public class GulimallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
}
