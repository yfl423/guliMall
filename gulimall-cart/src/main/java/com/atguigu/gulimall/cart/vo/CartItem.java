package com.atguigu.gulimall.cart.vo;

import lombok.ToString;

import java.util.List;
import java.math.BigDecimal;

@ToString
public class CartItem {

    private Long skuId;
    private String skuDefaultImg;
    private String skuTitle;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private Boolean check = true;
    private List<String> skuAttr;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getSkuDefaultImg() {
        return skuDefaultImg;
    }

    public void setSkuDefaultImg(String skuDefaultImg) {
        this.skuDefaultImg = skuDefaultImg;
    }

    public String getSkuTitle() {
        return skuTitle;
    }

    public void setSkuTitle(String skuTitle) {
        this.skuTitle = skuTitle;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        this.totalPrice= this.price.multiply(new BigDecimal(this.count));
        return totalPrice;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }
}
