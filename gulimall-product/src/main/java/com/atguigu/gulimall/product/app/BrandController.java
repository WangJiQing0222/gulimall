package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author wangjiqing
 * @email wangjiqing0222@163.com
 * @date 2022-12-11 15:50:01
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brand = brandService.getBrandsByIds(brandIds);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand/* , BindingResult result*/){
/*        if(result.hasErrors()){
            Map<String, String> map = new HashMap<>();
            //1、获取校验的错误结果
            result.getFieldErrors().forEach((item) -> {
                //获取错误信息
                String message = item.getDefaultMessage();
                //获取错误属性名
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(400, "提交的数据不合法").put("data", map);
        }else {
            brandService.save(brand);
        }*/

        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改品牌状态，
     * @param brand
     * @return
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
