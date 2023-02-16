package com.atguigu.gulimall.gulimallware;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.controller.PurchaseController;
import com.atguigu.gulimall.ware.controller.WareSkuController;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import io.netty.util.internal.StringUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class GulimallWareApplicationTests {

    @Autowired
    WareSkuController wareSkuController;

    @Autowired
    private PurchaseService purchaseService;

    @Test
    public void contextLoads() {
        ArrayList<Integer> integers = new ArrayList<>();
        boolean empty = StringUtils.isEmpty(integers);
        System.out.println(empty);

    }

}
