package com.begin.gulimall.coupon.service.impl;

import com.begin.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.begin.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.Query;

import com.begin.gulimall.coupon.dao.SeckillSessionDao;
import com.begin.gulimall.coupon.entity.SeckillSessionEntity;
import com.begin.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {


    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取秒杀活动及需要定时上架的商品
     *
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        //计算最近三天
        LocalDate now = LocalDate.now();
        LocalDate localDate1 = now.plusDays(2);

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;

        String start = LocalDateTime.of(now, min).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = LocalDateTime.of(localDate1, max).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", start, end));
        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationSkus(relationEntities);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

}