package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderOperateHistoryDao;
import com.atguigu.gulimall.order.entity.OrderOperateHistoryEntity;
import com.atguigu.gulimall.order.service.OrderOperateHistoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("orderOperateHistoryService")
public class OrderOperateHistoryServiceImpl extends ServiceImpl<OrderOperateHistoryDao, OrderOperateHistoryEntity> implements OrderOperateHistoryService {

    @Autowired
    WmsFeignService wmsFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderOperateHistoryEntity> page = this.page(
                new Query<OrderOperateHistoryEntity>().getPage(params),
                new QueryWrapper<OrderOperateHistoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 测试Seata分布式事务是否生效
     * @return
     */
    @GlobalTransactional
    @Transactional
    @Override
    public Boolean seata() {
        OrderOperateHistoryEntity entity = new OrderOperateHistoryEntity();
        entity.setCreateTime(new Date());
        entity.setOperateMan("WangJiQing");
        boolean flag = this.save(entity);

        R r = wmsFeignService.testSeata();
        if(r.getCode() == 0){
            System.out.println("远程调用wms测试成功");
        }

        int i = 1 / 0;
        return flag;
    }

}