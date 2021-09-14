package com.begin.gulimall.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;

//@SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {

        //彩虹表可能反推
        String s = DigestUtils.md5Hex("132456");

        //MD5不能直接进行密码的加密存储

        //盐值加密：随机值
        //"123456"+System.currentTimeMills()
//        String s1 = Md5Crypt.md5Crypt("123456".getBytes(StandardCharsets.UTF_8));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        //$2a$10$yKgUWF5yahmxt9WH4Don8.HnwFAjdTDvI/Dyuk5BQS84JZJEruNw.
        String encode = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$yKgUWF5yahmxt9WH4Don8.HnwFAjdTDvI/Dyuk5BQS84JZJEruNw.");

        System.out.println(encode+"=>"+matches);
    }

}
