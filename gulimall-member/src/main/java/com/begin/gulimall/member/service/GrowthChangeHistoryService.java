package com.begin.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 16:56:56
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

