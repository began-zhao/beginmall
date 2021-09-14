package com.begin.gulimall.product.vo;

import com.begin.gulimall.product.entity.SkuImagesEntity;
import com.begin.gulimall.product.entity.SkuInfoEntity;
import com.begin.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemVo {
    //1、sku基本信息获取 pms_sku_info
    SkuInfoEntity skuInfo;

    //是否有货
    boolean hasStock = true;

    //2、sku的图片信息获取 pms_sku_images
    List<SkuImagesEntity> images;

    //3、spu销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4、获取spu介绍
    SpuInfoDescEntity desp;

    //5、获取spu规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    //6、当亲商品秒杀优惠信息
    SeckillInfoVo seckillInfo;

}

