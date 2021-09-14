package com.begin.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserInfoTo {
    private Long userId;
    private String userKey;

    /*是否为临时用户*/
    private boolean tempUser=false;
}
