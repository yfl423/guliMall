package com.atguigu.gulimall.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserLoginVo {
    @NotEmpty(message = "用户名必须提交")
    private String userName;
    @NotEmpty(message = "密码必须提交")
    private String password;
}
