package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallOrderApplicationTests {

    @Autowired
    private OrderService orderService;

    @Test
    public void contextLoads() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setNote("大客户订单...");
        orderService.save(orderEntity);
    }

}
