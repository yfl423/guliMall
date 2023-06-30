package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignClient;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoResponeVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    ProductFeignClient productFeignClient;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer count) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(String.valueOf(skuId));
        if (!StringUtils.isEmpty(o)) {
            // 购物车里已经有该购物项
            cartOps.delete(String.valueOf(skuId));
            CartItem cartItem = JSON.parseObject(o, CartItem.class);
            cartItem.setCount(cartItem.getCount() + count);
            cartOps.put(String.valueOf(skuId), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            // 购物车里没有该购物项
            CartItem cartItem = new CartItem();
            cartItem.setSkuId(skuId);
            cartItem.setCount(count);
            cartItem.setCheck(true);
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = productFeignClient.getSkuInfo(skuId);
                SkuInfoResponeVo skuInfo = JSON.parseObject(JSON.toJSONString(r.get("skuInfo")), new TypeReference<>() {
                });
                cartItem.setSkuDefaultImg(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setSkuTitle(skuInfo.getSkuTitle());
            }, executor);

            CompletableFuture<Void> getSkuAttrTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttributes = productFeignClient.getSkuSaleAttributes(skuId);
                cartItem.setSkuAttr(skuSaleAttributes);
            }, executor);
            CompletableFuture.allOf(getSkuInfoTask, getSkuAttrTask).get();
            cartOps.put(String.valueOf(skuId), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(String.valueOf(skuId));
        CartItem cartItem = JSON.parseObject(o, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getMyCart() {
        Cart cart = new Cart();
        cart.setDiscount(new BigDecimal(0));
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            //  未登录状态，直接返回临时购物车即可
            List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            cart.setCartItems(cartItems);
        } else {
            // 登录状态，先查看用户的临时购物车
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            BoundHashOperations<String, Object, Object> tempCartOps = redisTemplate.boundHashOps(cartKey);
            List<Object> itemsList = tempCartOps.values();
            if (itemsList != null && itemsList.size() > 0) {
                // 临时购物车有购物项，因此要先把临时购物车的购物项合并到用户购物车
                BoundHashOperations<String, Object, Object> userCartOps = redisTemplate.boundHashOps(CART_PREFIX + userInfoTo.getUserId());
                itemsList.forEach(o -> {
                    String jsonString = (String) o;
                    CartItem cartItem = JSON.parseObject(jsonString, CartItem.class);
                    String s = (String) userCartOps.get(String.valueOf(cartItem.getSkuId()));
                    if (!StringUtils.isEmpty(s)) {
                        // 用户购物车里已经有该购物项,进行合并
                        userCartOps.delete(String.valueOf(cartItem.getSkuId()));
                        CartItem updatedCartItem = JSON.parseObject(s, CartItem.class);
                        updatedCartItem.setCount(updatedCartItem.getCount() + cartItem.getCount());
                        userCartOps.put(String.valueOf(updatedCartItem.getSkuId()), JSON.toJSONString(updatedCartItem));
                    } else {
                        // 用户购物车没有该购物项，直接添加到用户购物车
                        userCartOps.put(String.valueOf(cartItem.getSkuId()), o);
                    }
                });
                // 更新用户购物车后，清空临时购物车
                clearCart(cartKey);
                // 此时再返回用户购物车
                List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserId());
                cart.setCartItems(cartItems);
            } else {
                // 临时购物车为空 直接返回用户购物车
                List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserId());
                cart.setCartItems(cartItems);
            }
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItemBySkuId(skuId);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(String.valueOf(skuId), s);
    }

    @Override
    public void countChange(Long skuId, Integer count) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        if (count <= 0) {
            cartOps.delete(String.valueOf(skuId));
        } else {
            CartItem cartItem = getCartItemBySkuId(skuId);
            cartItem.setCount(count);
            String s = JSON.toJSONString(cartItem);
            cartOps.put(String.valueOf(skuId), s);
        }
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(String.valueOf(skuId));
    }

    /**
     * 获取用户购物车内所有被选中的购物项，并且调用远程查询获取当前购物项的最新价格
     * @return
     */
    @Override
    public List<CartItem> getAllCheckedItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> collect = cartItems.stream().filter(CartItem::getCheck).map(cartItem -> {
                // 购物车内的购物项价格可能是过时价格，必须重新去商品系统中获取最新价格
                BigDecimal latestPrice = productFeignClient.getLatestPrice(cartItem.getSkuId());
                cartItem.setPrice(latestPrice);
//                System.out.println(cartItem);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
    }


    /**
     * 指定购物车key，返回整个购物车，并转换为List<CartItem>
     *
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        List<Object> updatedItemsList = cartOps.values();
        List<CartItem> cartItems = new ArrayList<>();
        if (updatedItemsList != null && updatedItemsList.size() > 0) {
            cartItems = updatedItemsList.stream().map(o -> {
                String jsonString = (String) o;
                CartItem cartItem = JSON.parseObject(jsonString, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
        }
        return cartItems;
    }

    /**
     * 获取到我们要操作的购物车
     * 取决于每个请求进来后拦截器对其的判断，根据threadlocal中的userInfoTo中的信息判断这个请求要操作的是用户购物车还是游客购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey;
        if (userInfoTo.getUserId() != null) {
            // 用户已登陆
            cartKey = CART_PREFIX + userInfoTo.getUserId().toString();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        return cartOps;
    }
}
