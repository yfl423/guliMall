package com.atguigu.gulimall.member.exception;

public class PasswordNotMatchUsernameException  extends RuntimeException{
    public PasswordNotMatchUsernameException() {
        super("用户名或者密码输入错误");
    }
}
