package com.begin.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.member.entity.MemberEntity;
import com.begin.gulimall.member.exception.PhoneExsitException;
import com.begin.gulimall.member.exception.UsernameExistException;
import com.begin.gulimall.member.vo.MemberLoginVo;
import com.begin.gulimall.member.vo.MemberRegistVo;
import com.begin.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 16:56:56
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExsitException;

    void checkUserNameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

