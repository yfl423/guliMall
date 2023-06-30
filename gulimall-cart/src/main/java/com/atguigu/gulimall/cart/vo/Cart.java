package com.atguigu.gulimall.cart.vo;

import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 有些属性属于业务属性，是其他属性通过计算得到的，因此cart和cartItem不使用@Data而是自己重写get方法，
 * 同时不给予set方法对外提供，因为它们不是人为设置而是关联计算得出的
 */
@ToString
public class Cart {
    List<CartItem> cartItems = new ArrayList<>();
    Integer countNum;
    Integer countType;
    BigDecimal totalPrice;
    BigDecimal discount = new BigDecimal(0);

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public Integer getCountNum() {
        countNum = 0;
        if (cartItems.size() > 0){
            cartItems.forEach(cartItem -> {
                countNum += cartItem.getCount();
            });
        }
        return countNum;
    }

    public Integer getCountType() {
        return this.cartItems.size();
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = new BigDecimal(0);
        if (cartItems.size() > 0){
            for (CartItem cartItem : cartItems){
                if (cartItem.getCheck()){
                    total = total.add(cartItem.getTotalPrice());
                }
            }
        }
        this.totalPrice = total.subtract(this.discount);
        return this.totalPrice;
    }


    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }


}
