package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
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
    private CategoryService categoryService;

    /**
     * http://localhost:10000/ 或者http://localhost:10000/index.html
     * 都能访问首页
     * @param model
     * @return
     */
    @GetMapping({"/", "index.html"})
    public String indexPage(Model model){
        //TODO  1、查出所有的1级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categorys();

        //视图解析器进行拼串 prefix + 返回值 + .suffix
        //classpath:/templates/+ 返回值 + .html
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    /**
     * TODO OutOfDirectMemoryError解决
     * https://www.jianshu.com/p/ef35d3331d41   TCP/IP关闭连接
     * 堆外内存：https://juejin.cn/post/7132171543651549220
     * 获取2级分类、3级分类
     * @return
     */
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        //TODO 优化: 一次查出所有数据，减少数据库访问 修改lettuce导致的堆外内存移除bug
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     * 用于测试性能指标
     * @return
     */

    @ResponseBody
    @GetMapping("/hello")
    public String testDemo(){
        return "hello";
    }

}
