package com.begin.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.begin.gulimall.common.utils.HttpUtils;
import com.begin.gulimall.member.dao.MemberLevelDao;
import com.begin.gulimall.member.entity.MemberLevelEntity;
import com.begin.gulimall.member.exception.PhoneExsitException;
import com.begin.gulimall.member.exception.UsernameExistException;
import com.begin.gulimall.member.vo.MemberLoginVo;
import com.begin.gulimall.member.vo.MemberRegistVo;
import com.begin.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.Query;

import com.begin.gulimall.member.dao.MemberDao;
import com.begin.gulimall.member.entity.MemberEntity;
import com.begin.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {

        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        //设置默认等级
        memberEntity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        //通过保存
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        //昵称暂时设置为用户名
        memberEntity.setNickname(vo.getUserName());

        //加密后保存密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        //其他的默认信息


        //保存
        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExsitException {
        Integer mobile = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));

        if (mobile > 0) {
            throw new PhoneExsitException();
        }
    }

    @Override
    public void checkUserNameUnique(String username) throws UsernameExistException {

        Integer integer = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (integer > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        //1、去数据库查询
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct)
                .or().eq("mobile", loginacct));

        if (memberEntity == null) {
            //用户名不存在
            return null;

        } else {
            //获取到数据库的加密字段
            String passwordDb = memberEntity.getPassword();

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            //进行密码匹配
            boolean matches = encoder.matches(vo.getPassword(), passwordDb);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }

        }
    }

    /**
     * 社交账号登录
     *
     * @param socialUser
     * @return
     */
    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        //登录和注册合并逻辑
        String uid = socialUser.getUid();

        //1、判断当前社交用户是否已经登陆过系统
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //这个用户已经注册，更新令牌和过期时间
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            memberDao.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            //2、没有查到当前社交用户对应的记录，需要进行注册过程
            MemberEntity regist = new MemberEntity();
            try {
                //3、查询当前社交用户的社交账号信息
                Map<String, String> query = new HashMap<>();
                query.put("access_token", socialUser.getAccess_token());
                query.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    //当前社交账号对应昵称
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");

                    //参照返回数据获取所有其他信息
                    regist.setNickname(name);
                    regist.setGender("m".equals(gender) ? 1 : 0);
                }
            } catch (Exception e) {

            }
            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            memberDao.insert(regist);
            return regist;
        }
    }

}