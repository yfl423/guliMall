package com.atguigu.common.exception;

public class NoStockException extends RuntimeException {
    public NoStockException(Long skuId) {
        super(skuId+"号商品没有库存了");
    }
}
