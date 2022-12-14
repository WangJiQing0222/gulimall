package com.atguigu.gulimall.coupon;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.service.CouponService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallCouponApplicationTests {

    @Autowired
    private CouponService couponService;

    @Test
    public void contextLoads() {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setAmount(BigDecimal.valueOf(100));

        couponService.save(couponEntity);

        System.out.println("success");
    }

}
