package com.atguigu.gulimallsearch.controller;

import com.atguigu.gulimallsearch.service.MallSearchService;
import com.atguigu.gulimallsearch.vo.SearchParam;
import com.atguigu.gulimallsearch.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * @param param 自动将页面提交过来的所有请求查询参数封装成指定的对象
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        //根据页面传递过来的查询参数，去es中检索商品
        param.set_queryString(request.getQueryString());

        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);

        System.out.println("-------------------" + result);
        return "list1";
    }
}
