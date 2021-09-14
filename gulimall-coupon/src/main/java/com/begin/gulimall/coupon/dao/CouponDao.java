package com.begin.gulimall.coupon.dao;

import com.begin.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 16:30:38
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
