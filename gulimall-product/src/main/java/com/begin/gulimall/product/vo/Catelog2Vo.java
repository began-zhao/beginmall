package com.begin.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 2级分类vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {
    private String catalog1Id;//1级父分类id
    private List<Catalog3Vo> catalog3List;//三级子分类
    private String id;
    private  String name;

    /**
     * 三级分类vo
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class  Catalog3Vo{
        private String catalog2Id;//父分类，2级分类ID
        private String id;
        private String name;
    }
}
