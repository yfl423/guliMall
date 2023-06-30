package com.atguigu.gulimall.member.exception;

public class MobileNotOnlyException extends RuntimeException {
    public MobileNotOnlyException() {
        super("手机号已注册");
    }
}
