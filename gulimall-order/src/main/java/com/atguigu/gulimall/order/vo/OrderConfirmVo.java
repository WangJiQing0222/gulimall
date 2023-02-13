package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要用的数据
 */
@Data
public class OrderConfirmVo {
    //收货地址列表
    List<MemberAddressVo> address;
    //所有选中的购物项
    List<OrderItemVo> items;

    //发票记录...

    //积分 优惠卷信息
    Integer integration;

    //库存skuId, true/false
    Map<Long, Boolean> stocks;

    //防重令牌
    String orderToken;

    public Integer getCount(){
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        System.out.println("++++++++++++++++计算购物车商品数量" + items);

        return i;
    }

    //订单总额
    BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }

        return sum;
    }

    //应付价格
    BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
