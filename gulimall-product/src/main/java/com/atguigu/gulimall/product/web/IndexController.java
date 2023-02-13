package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * http://localhost:10000/ 或者http://localhost:10000/index.html
     * 都能访问首页
     * @param model
     * @return
     */
    @GetMapping({"/", "index.html"})
    public String indexPage(Model model){
        //TODO  1、查出所有的1级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categorys();

        //视图解析器进行拼串 prefix + 返回值 + .suffix
        //classpath:/templates/+ 返回值 + .html
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    /**
     * TODO OutOfDirectMemoryError解决
     * https://www.jianshu.com/p/ef35d3331d41   TCP/IP关闭连接
     * 堆外内存：https://juejin.cn/post/7132171543651549220
     * 获取2级分类、3级分类
     * @return
     */
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        //TODO 优化: 一次查出所有数据，减少数据库访问 修改lettuce导致的堆外内存移除bug
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     * redisson看门狗
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String testDemo() throws InterruptedException {
        RLock lock = redissonClient.getLock("my-lock");
        //默认情况下，看门狗的检查锁的超时时间是 30秒钟 业务
        //业务耗时 > 默认30秒,                          自动续时
        //业务耗时 < 默认30秒,提前结束占锁
//        lock.lock();

        // 加锁以后10秒钟                                 自动解锁
        //业务耗时 > 默认30秒                      注意: 不会自动续时
        //业务耗时 < 默认30秒,提前结束占锁
        // 无需调用unlock方法手动解锁
//        lock.lock(10, TimeUnit.SECONDS);


        //10秒后自动解锁,最多等待20秒,业务耗时超过20秒,也会解锁
        lock.tryLock(20, 10, TimeUnit.SECONDS);
        log.debug("加锁成功...");

        //业务执行
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        lock.unlock();
        log.debug("解锁成功...");

        return "hello";
    }

    /**保证能读到最新数据 写锁加锁期间，读锁（共享锁）与写锁（排它锁）互斥无法读取，写锁解锁，读锁才可读取
     * 读写锁测试
     * 读 + 读 并发
     * 写 + 写 互斥
     * 读 + 写 互斥
     * @return
     */
    @GetMapping("/write")
    @ResponseBody
    public String write(){

        RReadWriteLock wlock = redissonClient.getReadWriteLock("writeLock");
        RLock rLock = wlock.writeLock();
        rLock.lock();
        log.debug("加写锁成功...");

        String s = "222";
        try {
            redisTemplate.opsForValue().set("writeReadLock", s);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            log.debug("写锁解锁成功...");
            rLock.unlock();
        }

        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String read(){
        RReadWriteLock wlock = redissonClient.getReadWriteLock("writeLock");
        RLock rLock = wlock.readLock();
        rLock.lock();
        log.debug("加读锁成功...");

        String s;
        try {
            s = redisTemplate.opsForValue().get("writeReadLock");
        }finally {
            rLock.unlock();
            log.debug("读锁解锁成功...");
        }

        return s;
    }

    /**
     * 闭锁
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch");
        countDownLatch.trySetCount(6);

        countDownLatch.await();//等待count为0

        return "6个班的学生都走完了";
    }

    @GetMapping("/go/{id}")
    @ResponseBody
    public String lockDoor(@PathVariable("id") Integer id){
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch");
        countDownLatch.countDown();//计数减1

        return id + "班的人都走完了";
    }

    /**
     * 信号量
     * 3车位,获取车位 分布式限流
     * @return
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
//        park.acquire();//信号量为0会阻塞
        boolean b = park.tryAcquire();//有返回值

        /*if(b){
            //执行业务
        }*/

        return "ok=>"+b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go(){
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();

        return "ok";
    }
}
