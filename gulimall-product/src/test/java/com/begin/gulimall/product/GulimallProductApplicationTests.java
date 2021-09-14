package com.begin.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.begin.gulimall.product.config.MyRedissonConfig;
import com.begin.gulimall.product.dao.AttrGroupDao;
import com.begin.gulimall.product.dao.SkuSaleAttrValueDao;
import com.begin.gulimall.product.entity.BrandEntity;
import com.begin.gulimall.product.service.BrandService;
import com.begin.gulimall.product.service.CategoryService;
import com.begin.gulimall.product.vo.SkuItemSaleAttrVo;
import com.begin.gulimall.product.vo.SkuItemVo;
import com.begin.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Test
    public void test(){
//        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(100L, 225L);
//        System.out.println(attrGroupWithAttrsBySpuId.toString());

        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(13L);
        System.out.println(saleAttrsBySpuId);
    }


    @Test
    void redisson(){
        System.out.println(redissonClient);
    }

    @Test
    void testStringRedisTemplate(){

        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        ops.set("hello", UUID.randomUUID().toString());

        String hello = ops.get("hello");

        System.out.println("之前存的数据为："+hello);
    }

    @Test
    void testFindPath()
    {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();

//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功");
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        list.forEach((item) -> System.out.println(item));
    }

}
