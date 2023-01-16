package com.atguigu.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo{
    private String catelogName;//分类名
    private String groupName;  //分组名
    private Long[] catelogPath;//分类完整路径
}
