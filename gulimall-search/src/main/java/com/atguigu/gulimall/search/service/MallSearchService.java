package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    /**
     *
     * @param param 检索参数
     * @return 返回检索结果
     */
    SearchResult search (SearchParam param);
}
