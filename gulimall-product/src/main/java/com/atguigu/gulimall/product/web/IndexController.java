package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;


    @GetMapping({"/", "index.html"})
    public String getIndexPage(Model model) {
        List<CategoryEntity> firstLevelCategories = categoryService.getXLevelCatelog(1);

        model.addAttribute("categories", firstLevelCategories);

        return "index";
    }

    /**
     * index/catalog.json
     */
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catalog2Vo>> getSubCatlogs() {
        List<CategoryEntity> level1Catelogs = categoryService.getXLevelCatelog(1);

        Map<String, List<Catalog2Vo>> subCatlogs = categoryService.getSubCatlogs(level1Catelogs);

        return subCatlogs;
    }
}
