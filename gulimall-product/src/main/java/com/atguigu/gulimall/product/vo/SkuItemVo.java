package com.atguigu.gulimall.product.vo;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import java.util.List;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SkuItemVo {
    // 1. sku基本信息获取
    private SkuInfoEntity info;
    // 有货无货
    private boolean hasStock = true;
    // 2. sku的图片信息
    private List<SkuImagesEntity> images;
    // 3. spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttrs;
    // 4. 获取spu的介绍
    private SpuInfoDescEntity spuDesc;
    // 5. 获取spu的规格参数
    private List<SpuItemAttrGroup> groupAttrs;
    // 6. 秒杀信息
    private SeckillInfoTo seckillInfo;

}
