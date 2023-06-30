package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;


@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @RequestMapping("/list.html")
    public String list(SearchParam param, Model model, HttpServletRequest httpServletRequest) {
        // 封装所有查询条件
        String queryString = httpServletRequest.getQueryString();
        param.set_queryString(queryString);

        SearchResult res = mallSearchService.search(param);
        model.addAttribute("result", res);
//        System.out.println(res);
        return "list";
    }
}
