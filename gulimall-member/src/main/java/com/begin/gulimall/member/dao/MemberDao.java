package com.begin.gulimall.member.dao;

import com.begin.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 16:56:56
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
