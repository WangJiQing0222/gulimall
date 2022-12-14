package com.atguigu.gulimall.gulimallware;

import com.atguigu.gulimall.ware.controller.PurchaseController;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallWareApplicationTests {

    @Autowired
    private PurchaseService purchaseService;

    @Test
    public void contextLoads() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setAmount(BigDecimal.valueOf(22));

        purchaseService.save(purchaseEntity);

    }

}
