package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class CategoryServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void findCatelogPath() {
        Long[] path = categoryService.findCatelogPath(225L);
        log.debug("path:{}", path);
    }

    @Test
    public void redisTest(){
        redisTemplate.opsForValue().set("haha", "nihaoa");

        String haha = redisTemplate.opsForValue().get("haha");

        System.out.println("++++++++++++" + haha);
    }
}