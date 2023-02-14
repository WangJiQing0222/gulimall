package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


/**
 * 秒杀商品的定时上架；
 *      每天晚上3点：上架最近三天需要秒杀的商品。
 *      当天00：80：00-23：59：59
 *      明天88：88：88-23:59:59
 *      后天00：e8:88-23:59:59
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";

    //TODO 幂等性处理
    @Scheduled(cron = "*/3 * * * * ?")//秒 分 时 日 月 周
    public void uploadSeckillSkuLatest3Days(){

        //分布式锁。锁的业务执行完成，状态经更新完成。释放锁以后。其他人获取到就会拿到最新的状态。
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            //1、重复上架无需处理
            log.debug("上架的秒杀商品信息。。。");
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }
}
