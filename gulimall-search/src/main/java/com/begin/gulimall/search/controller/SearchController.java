package com.begin.gulimall.search.controller;

import com.begin.gulimall.search.service.MallSearchService;
import com.begin.gulimall.search.vo.SearchParam;
import com.begin.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {


    @Resource
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request)
    {
        param.set_queryString(request.getQueryString());
        //根据页面传递进来的的查询参数，去es中检索商品
        SearchResult result= mallSearchService.search(param);
        model.addAttribute("result",result);
        return  "list";
    }
}
