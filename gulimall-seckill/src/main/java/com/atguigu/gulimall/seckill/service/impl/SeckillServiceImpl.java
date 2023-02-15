package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;


    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private final String SECKILL_CACHE_PREFIX = "seckill:skus";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1、扫描最近三天需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            //上架商品
            List<SeckillSessionsWithSkus> sessionData = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到redis
            //1、缓存活动信息
            saveSessionInfos(sessionData);
            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    public List<SeckillSkuRedisTo> blockHandler(){
        log.error("getCurrentSeckillSkusResource被限流了。。。");
        return null;
    }

    @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前属于哪个秒杀场次
        long time = new Date().getTime();

        try(Entry entry = SphU.entry("seckillSkus")) { // 资源名seckillSkus
            Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");

            for (String key : keys) {
                String replace = key.replace(SESSION_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                long start = Long.parseLong(s[0]);
                long end = Long.parseLong(s[1]);

                if (time >= start && time <= end) {
                    //2、获取这个秒杀场次需要的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_CACHE_PREFIX);
                    List<String> list = hashOps.multiGet(range);

                    if(list != null){
                        List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                            SeckillSkuRedisTo redis = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
                            // redisTo.setRandomCode(null);当前秒杀开始需要随机码
                            return redis;
                        }).collect(Collectors.toList());
                        System.out.println("----------------秒杀已获取商品信息");
                        return collect;
                    }
                    break;
                }
            }

        }catch (BlockException e){
            log.debug("资源被限流,{}", e.getMessage());
        }

        System.out.println("----------------秒杀未获取商品信息");

        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1、找到所有需要秒杀的商品的key信息---seckill:skus
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_CACHE_PREFIX);

        Set<String> keys = hashOps.keys();
        if(keys != null && keys.size() > 0){
            //1_1 正则表达式进行匹配
            String regexp = "\\d_" + skuId;
            for (String key : keys) {
                if(Pattern.matches(regexp, key)){
                    //从Redis中取出数据来
                    String json = hashOps.get(key);
                    //进行反序列化
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    //随机码
                    long currentTime = new Date().getTime();
                    if(currentTime >= redisTo.getStartTime() && currentTime <= redisTo.getEndTime()){
                    }else {//不在当前秒杀时间，不返回随机码
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }

        }


        return null;
    }

    /**
     * T0D0 上架秒杀商品的时候，每一个数据都有过期时间。
     * T0D0 秒杀后续的流程，简化了收货地址等信息。
     * 当前商品进行秒杀（秒杀开始）
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        long s1 = System.currentTimeMillis();

        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();

        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_CACHE_PREFIX);

        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        }else {
            SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long currentTime = new Date().getTime();

            //1、校验时间的合法性
            if (currentTime >= startTime && currentTime <= endTime) {
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                //2、校验随机码和商品id
                if (randomCode.equals( key) && skuId.equals(killId)) {
                    //3、验证购物数量是否合理
                    if(num <= redisTo.getSeckillLimit()){
                        //4、验证这个人是否已经购买过。幂等性；如果只要秒杀成功，就去占位。userId_sessionId_skuId
                        //SETNX
                        String redisKey = respVo.getId() + "_" + skuId;
                        //自动过期
                        long ttl = endTime - currentTime;

                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);

                        if (aBoolean) {
                            //4、占位成功说明从来没有买过,分布式锁(获取信号量-1)
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);

                            try {
                                //120ms 20ms
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                //秒杀成功
                                //快速下单，发送给MQ消息，10ms
                                if (b) {
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo orderTo = new SeckillOrderTo();
                                    orderTo.setOrderSn(timeId);
                                    orderTo.setNum(num);
                                    orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                    orderTo.setMemberId(respVo.getId());
                                    orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                    orderTo.setSkuId(redisTo.getSkuId());
                                    rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);

                                    long s2 = System.currentTimeMillis();
                                    log.info("耗时..." + (s2 - s1));
                                    return timeId;
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }else {
                            //说明已经买过了
                            return null;
                        }
                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }

        }


        return null;
    }

    /**
     * 缓存秒杀活动信息
     *
     * @param sessionData
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessionData) {
        if(sessionData != null)
        sessionData.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;

            //缓存活动信息
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationSkus().stream().map(item -> {
                    return item.getPromotionSessionId() + "_" + item.getSkuId();
                }).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    /**
     * 缓存秒杀活动所关联的商品信息
     *
     * @param sessionData
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessionData) {
        sessionData.stream().forEach(session -> {
            //准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                //4、随机码？seckill&skuId=1&key=ajfda;l
                String token = UUID.randomUUID().toString().replace("-", "");

                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString())) {
                    //缓存商品
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    //1、sku的基本数据
                    R r = productFeignService.info(seckillSkuVo.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(skuInfo);
                    }

                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);

                    //3、设置上当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    //4、设置商品的随机码
                    redisTo.setRandomCode(token);

                    //序列化json格式存入Redis中
                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);

                    //如果当前这个场次的商品库存信息已经上架就不需要上架
                    //5、使用库存作为分布式的信号量，限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }
}
