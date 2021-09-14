package com.begin.gulimall.coupon.service.impl;

import com.begin.gulimall.common.to.MemberPrice;
import com.begin.gulimall.common.to.SkuReductionTo;
import com.begin.gulimall.coupon.entity.MemberPriceEntity;
import com.begin.gulimall.coupon.entity.SkuLadderEntity;
import com.begin.gulimall.coupon.service.MemberPriceService;
import com.begin.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.Query;

import com.begin.gulimall.coupon.dao.SkuFullReductionDao;
import com.begin.gulimall.coupon.entity.SkuFullReductionEntity;
import com.begin.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;
    @Autowired
    MemberPriceService memberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo reductionTo) {
        //1、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_redication\sms_member_price
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(reductionTo.getSkuId());
        skuLadderEntity.setFullCount(reductionTo.getFullCount());
        skuLadderEntity.setDiscount(reductionTo.getDiscount());
        skuLadderEntity.setAddOther(reductionTo.getCountStatus());
        if (reductionTo.getFullCount()>0) {
            skuLadderService.save(skuLadderEntity);
        }

        //2、保存满减信息
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(reductionTo, skuFullReductionEntity);
        if (reductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1) {
            this.save(skuFullReductionEntity);
        }

        //3、会员价格
        List<MemberPrice> memberPrice = reductionTo.getMemberPrice();
        if (memberPrice != null && memberPrice.size() > 0) {
            List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(reductionTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).filter(item->{
                return item.getMemberPrice().compareTo(new BigDecimal("0"))==1;
            }).collect(Collectors.toList());

            memberPriceService.saveBatch(collect);
        }


    }

}