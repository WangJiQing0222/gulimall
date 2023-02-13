package com.atguigu.gulimall.cart.to;

import lombok.Data;

@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;

    private Boolean tempUser = false;//默认是临时用户
}
