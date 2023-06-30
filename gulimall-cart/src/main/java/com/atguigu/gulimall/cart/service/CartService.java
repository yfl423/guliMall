package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    /**
     * 在当前购物车中添加一个购物项
     *
     * @param skuId
     * @param count
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer count) throws ExecutionException, InterruptedException;

    /**
     * 根据skuId获取当前购物车的一个购物项
     *
     * @param skuId
     * @return
     */
    CartItem getCartItemBySkuId(Long skuId);

    /**
     * 获取当前购物车（可能涉及到临时购物车和用户购物购物车的合并）
     *
     * @return
     */
    Cart getMyCart();

    /**
     * 清除购物车
     *
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 勾选购物项
     *
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 增加或减少购物项数量
     * @param count
     */
    void countChange(Long skuId ,Integer count);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteItem(Long skuId);

    /**
     * 获取用户购物车中所有被选中的购物项
     * @return
     */
    List<CartItem> getAllCheckedItems();
}
