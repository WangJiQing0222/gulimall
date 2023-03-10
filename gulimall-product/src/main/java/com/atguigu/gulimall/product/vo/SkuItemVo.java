package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    //1、sku基本信息获取 pms_sku_info
    private SkuInfoEntity info;

    private boolean hasStock = true;

    //2、sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;

    //3、获取spu的销售属性组合（自己封装）
    private List<SkuItemSaleAttrVo> saleAttr;

    //4、获取spu的介绍
    private SpuInfoDescEntity desc;

    //5、获取spu的规格参数信息（自己封装）
    private List<SpuItemAttrGroupVo> groupAttrs;

    //6、当前商品的秒杀优惠信息
    private SeckillInfoVo seckillInfoVo;
}

