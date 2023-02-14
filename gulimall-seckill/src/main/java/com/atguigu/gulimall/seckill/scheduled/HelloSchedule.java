package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 定时任务
 *      1、EnableScheduling开启定时任务
 *      2、@Scheduled开启一个定时任务
 *      3、自动配置类TaskSchedulingAutoConfiguration
 * 异步任务
 *      1、EnableAsync开启异步任务功能
 *      2、QAsync给希望异步执行的方法上标注
 *      3、自动配置类TaskExecutionAutoConfiguration属性绑定在TaskExecutionProperties
 */

@Slf4j
@Component
@EnableScheduling
@EnableAsync
public class HelloSchedule {

    /**
     * 1、Spring中6位组成.不允许第7位的年
     * 2、在周几的位置，1-7代表周一到周日；
     * MON-SUN
     * 3、定时任务不应该阻寨。默认是阻赛的
     *      1)、可以让业务运行以异步的方式，自己提交到线程池
     *          CompLetabLeFuture.runAsync{()->
     *          xxxxService.hello(),executor)}
     *
     *      2)、支持定时任务线程池；设置TaskSchedulingProperties:
     *          spring.task.scheduling.pool.size=5
     *      3)、让定时任务异步执行；设置 TaskExecutionAutoConfiguration
     *          spring.task.execution.pool.core-size=20
     *          spring.task.execution.pool.max-size=50
     * 异步任务
     */
    @Scheduled(cron = "* * * * * *")
    @Async//默认coreSize = 8; maxSize = 2147483647;
    public void hello() throws InterruptedException {
        log.debug("hello...");
        System.out.println("lalala-->线程id：" + Thread.currentThread().getId());
        Thread.sleep(3000);

    }
}
