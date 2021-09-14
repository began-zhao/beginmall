package com.begin.gilimall.auth.feign;

import com.begin.gilimall.auth.vo.SocialUser;
import com.begin.gilimall.auth.vo.UserLoginVo;
import com.begin.gilimall.auth.vo.UserRegistVo;
import com.begin.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    /**
     * 远程注册方法
     *
     * @param vo
     * @return
     */
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    /**
     * 远程登录方法
     *
     * @param vo
     * @return
     */
    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
