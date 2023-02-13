package com.atguigu.gulimallsearch.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-pruduct")
public interface ProductFeignService {
    /**
     * 属性信息
     */
    @RequestMapping("/product/attr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);

    /**
     * 品牌信息
     */
    @GetMapping("/infos")
    R brandsInfo(@RequestParam("brandIds") List<Long> brandIds);
}
