package com.atguigu.gulimallsearch.service;

import com.atguigu.gulimallsearch.vo.SearchParam;
import com.atguigu.gulimallsearch.vo.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param param 检索的查询参数
     * @return 返回检索的结果，里面包含页面需要的所有信息
     */
    SearchResult search(SearchParam param);
}
