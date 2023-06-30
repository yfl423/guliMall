package com.atguigu.common.exception;

/***
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *  10: 通用
 *      001：参数格式校验
 *      002: 短信验证码频率太高
 *  11: 商品
 *  12: 订单
 *      001： 重复提交
 *      002: 验价失败
 *      003: 没有库存
 *  13: 购物车
 *  14: 物流
 *  15: 用户
 *      001：用户已注册
 *      002：手机号已注册
 *      003：用户名不存在
 *      004：用户名或密码输入错误
 */
public enum BizCodeEnume {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VAILD_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "短信验证码频率太高,稍后再试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    ORDER_REPEAT_SUBMIT_EXCEPTION(12001, "重复提交"),
    PRICE_NOT_SAME_EXCEPTION(12002, "订单实际价格可能发生了变化"),
    NO_STOCK_EXCEPTION(12003, "商品库存不足"),
    USER_EXIST_EXCEPTION(15001, "用户已注册"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已注册"),
    USERNAME_NOT_EXIST_EXCEPTION(15003, "用户名不存在"),
    PASSWORD_NOT_MATCH_USERNAME_EXCEPTION(15004, "用户名或密码输入错误");


    private int code;
    private String msg;

    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
