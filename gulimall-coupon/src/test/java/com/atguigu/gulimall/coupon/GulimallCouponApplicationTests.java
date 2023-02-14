package com.atguigu.gulimall.coupon;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.service.CouponService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
//@RunWith(SpringRunner.class)
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


    @Test
    public void computeTime(){
        LocalDate now = LocalDate.now();
        LocalDate now1 = now.plusDays(1);
        LocalDate now2 = now.plusDays(2);

        System.out.println(now1);
        System.out.println(now2);

        LocalDateTime start = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(now2, LocalTime.MAX);

        System.out.println(start);
        System.out.println(end);
    }


    @Test
    public void testTime(){
        System.out.println(startTime());
        System.out.println(endTime());
    }

    /**
     * 起始时间 日期 + 00：00
     * @return
     */
    private String startTime(){
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        String startFormat = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return startFormat;
    }

    /**
     * 结束时间 日期+2天 + 23:59:59.999999999
     * @return
     */
    private String endTime(){
        LocalDateTime end = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX);
        String endFormat = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return endFormat;
    }

}
