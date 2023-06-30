package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    RBloomFilter<Long> bloomFilter;

    /**
     * 展示当前sku的详情
     *
     * @param skuId
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/{skuId}.html")
    public String getIndexPage(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        if (bloomFilter.contains(skuId)) {
            SkuItemVo skuItemVo = skuInfoService.item(skuId);
            model.addAttribute("item", skuItemVo);
            return "item";
        } else {
            return "error";
        }
    }
}
