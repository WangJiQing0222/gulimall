package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn;//订单号

    private List<OrderItemVo> locks;//需要锁住的所有库存信息 主要锁住的是每个商品的skuId有多少个count
}