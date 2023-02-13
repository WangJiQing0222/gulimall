package com.atguigu.gulimallsearch.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面可能传递过来的所有查询条件
 *  catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1&brandId=1&brandId=2
 */
@Data
public class SearchParam {
    private String keyword;//页面传递过来的全文匹配关键字
    private Long catalog3Id;//三级分类id

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;//排序条件
    /**
     * 好多的过滤条件
     * hasStock(是否有货)、skuPrice区间、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     * brandId=1
     * attrs=2_5寸:6寸
     */

    private Integer hasStock;//是否有货 （0无货）（1有货）
    private String skuPrice;//价格区间插叙
    private List<Long> brandId;//品牌id，可以多选
    private List<String> attrs;//属性
    private Integer pageNum = 1;//页码，加上默认值，否则NPE

    private String _queryString;//原生的所有查询条件
}
