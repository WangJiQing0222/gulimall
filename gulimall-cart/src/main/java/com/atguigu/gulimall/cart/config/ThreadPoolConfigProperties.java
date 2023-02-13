package com.atguigu.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {
    //核心线程数
    private Integer coreThread;
    //最大线程数
    private Integer maxThread;
    //空闲线程空闲时存活时间 空闲线程=最大线程数 - 核心线程数
    private Integer maxAliveTime;
}
