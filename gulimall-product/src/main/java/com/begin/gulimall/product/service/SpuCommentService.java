package com.begin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 14:44:46
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

