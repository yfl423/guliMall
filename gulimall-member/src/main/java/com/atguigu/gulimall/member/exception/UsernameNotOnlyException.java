package com.atguigu.gulimall.member.exception;

public class UsernameNotOnlyException extends RuntimeException{

    public UsernameNotOnlyException() {
        super("用户名已存在");
    }
}
