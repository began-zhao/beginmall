package com.begin.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.begin.gulimall.product.feign.CouponFeignService;
import com.begin.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import com.begin.gulimall.product.entity.SpuInfoEntity;
import com.begin.gulimall.product.service.SpuInfoService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.R;



/**
 * spu信息
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 15:46:24
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    @Lazy
    private CouponFeignService couponFeignService;
    @GetMapping("/test")
    public R test(){
        R test = couponFeignService.test();
        return R.ok().put("data", test);
    }

    ///product/spuinfo/{spuId}/up
    /**
     * 商品上架功能
     * @param spuId
     * @return
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId")Long spuId){

        spuInfoService.up(spuId);
        return R.ok();
    }

    @GetMapping("/{skuId}/getSpuInfo")
    public R getSpuInfoBySkuId(@PathVariable("skuId")Long skuId){

        SpuInfoEntity entity= spuInfoService.getSpuInfoBySkuId(skuId);

        return R.ok().setData(entity);
    }

    /**
     * 列表
     * /product/spuinfo/list
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuSaveVo vo){
//		spuInfoService.save(spuInfo);
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);


        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
