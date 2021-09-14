package com.begin.gulimall.product.vo;

import lombok.Data;

/**
 * @author 83456
 */
@Data
public class AttrRespVo extends AttrVo{

    /**
     * 所属分类
     * 所属分组
     *
     */
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}
