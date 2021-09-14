package com.begin.gulimall.product.dao;

import com.begin.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 14:44:47
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
