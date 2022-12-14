package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author wangjiqing
 * @email wangjiqing0222@163.com
 * @date 2022-12-11 17:42:05
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
