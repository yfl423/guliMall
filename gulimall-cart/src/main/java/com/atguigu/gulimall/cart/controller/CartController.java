package com.atguigu.gulimall.cart.controller;

import java.util.List;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class CartController {
    @Autowired
    CartService cartService;

    /**
     * 展示购物车
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
        Cart myCart = cartService.getMyCart();
        model.addAttribute("cart", myCart);
        return "cartList";
    }

    /**
     * 将商品加入购物车
     * 如果使用页面转发的方式转发到加入成功的页面，就会有重复提交的缺陷（转发请求的数据始终有效）
     * 为此，使用reidrectAttribute，并使用重定向到成功页面，这样数据只一次有效
     *
     * @param count
     * @param skuId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("count") Integer count, @RequestParam("skuId") Long skuId, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, count);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCart/success";
    }

    @GetMapping("/addToCart/success")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.getCartItemBySkuId(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countChange")
    public String countChange(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count) {
        cartService.countChange(skuId, count);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @ResponseBody
    @GetMapping("/checkedCartsItems")
    public List<CartItem> getAllCheckedItems() {
        List<CartItem> cartItems = cartService.getAllCheckedItems();
        return cartItems;
    }
}
