package com.begin.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.begin.gulimall.common.valid.AddGroup;
import com.begin.gulimall.common.valid.UpdateGroup;
import com.begin.gulimall.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.begin.gulimall.product.entity.BrandEntity;
import com.begin.gulimall.product.service.BrandService;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.R;


/**
 * 品牌
 *
 * @author zzg
 * @email 834561898@gmail.com
 * @date 2021-08-07 15:46:25
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
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {


        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 信息
     */
    @RequestMapping("/infos")
    public R info(@RequestParam("brandId") List<Long> brandId) {

        List<BrandEntity> brand = brandService.getBrandByIds(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand) {

//        if (bindingResult.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            //获取校验的结果
//            bindingResult.getFieldErrors().forEach((item) -> {
//                //获取到错误提示
//                String message = item.getDefaultMessage();
//                String field = item.getField();
//                map.put(field, message);
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        } else {
//            brandService.save(brand);
//        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }
    /**
     * 修改状态
     */
    @RequestMapping("/updateStatus")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
