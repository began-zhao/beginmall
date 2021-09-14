package com.begin.gulimall.search.service;

import com.begin.gulimall.search.vo.SearchParam;
import com.begin.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param param 检索的所有参数
     * @return 检索的结果
     */
    SearchResult search(SearchParam param);
}
