package com.begin.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.begin.gulimall.common.exception.NoStockException;
import com.begin.gulimall.common.to.mq.OrderTo;
import com.begin.gulimall.common.to.mq.StockDetailTo;
import com.begin.gulimall.common.to.mq.StockLockedTo;
import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.begin.gulimall.ware.entity.WareOrderTaskEntity;
import com.begin.gulimall.ware.feign.OrderFeignService;
import com.begin.gulimall.ware.feign.ProductFeignService;
import com.begin.gulimall.ware.service.WareOrderTaskDetailService;
import com.begin.gulimall.ware.service.WareOrderTaskService;
import com.begin.gulimall.ware.vo.*;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.Query;

import com.begin.gulimall.ware.dao.WareSkuDao;
import com.begin.gulimall.ware.entity.WareSkuEntity;
import com.begin.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    /**
     * 解锁库存方法
     *
     * @param skuId
     * @param wareId
     * @param num
     */
    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        wareSkuDao.unLockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//2:已解锁
        orderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);

            //远程查询sku的名字，如果失败。整个事务无需回滚
            try {
                R info = productFeignService.info(skuId);

                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    entity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            this.baseMapper.insert(entity);
        }
        this.baseMapper.addStock(skuId, wareId, skuNum);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            //查询当前总库存量
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     * (rollbackFor = NoStockException.class)
     * 默认运行时异常会回滚
     * <p>
     * 库存解锁的场景
     * 1）、下订单成功，订单过期没有支付被系统自动取消 ，被用户手动取消。都要解锁库存
     * 2）、下订单成功，库存锁定，接下来业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单的详情。
         * 追溯
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);


        //按照下单的收货地址，找到一个就近仓库，锁定库存
        //1、找到，每个商品在那个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        Boolean allLock = true;
        //2、锁定库存
        for (SkuWareHasStock has : collect) {
            Boolean skuStocked = false;
            Long skuId = has.getSkuId();
            List<Long> wareIds = has.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有任何仓库有这个商品的库存
                //直接抛出自定义异常
                throw new NoStockException(skuId);
            }
            //没有问题，逐个仓库进行扣除
            for (Long wareId : wareIds) {
                //成功就返回1，否则就是0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, has.getNum());
                //1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
                //2、如果锁定失败。前面保存的工作单信息就回滚了，发送出去的消息，即使要解锁记录由于去数据库查不到ID，所以不用解锁
                if (count == 1) {
                    skuStocked = true;
                    //TODO 告诉MQ保存成功
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                    detailEntity.setSkuId(skuId);
                    detailEntity.setSkuName("");
                    detailEntity.setSkuNum(has.getNum());
                    detailEntity.setTaskId(taskEntity.getId());
                    detailEntity.setWareId(wareId);
                    detailEntity.setLockStatus(1);
                    orderTaskDetailService.save(detailEntity);

                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, stockDetailTo);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    //只发Id不行，防止回滚以后找不到数据
                    lockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);

                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库

                }
            }
            if (skuStocked == false) {
                //当前商品没有锁住
                throw new NoStockException(skuId);
            }
        }

        //3、全部锁定成功
        return true;
    }

    @Override
    public void unLockStock(StockLockedTo to) {
        //库存工作单的Id
        Long id = to.getId();
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //解锁
        //1、查询数据库关于这个订单的锁定库存信息
        //有:证明库存锁定成功了
        //   解锁：订单情况。
        //      1、没有这个订单，必须解锁
        //      2、有这个订单，不是解锁库存
        //          订单状态：已取消，解锁库存
        //                   没取消，不能解锁
        //
        //没有：库存锁定失败了，库存回滚。这种情况无需解锁
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解锁，查订单情况
            Long toId = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(toId);
            String orderSn = taskEntity.getOrderSn();
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = orderStatus.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //订单不存在或者订单已经被取消，解锁库存
                    if (byId.getLockStatus() == 1) {
                        //判断库存工作单状态是否可解锁 1：已锁定未解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒绝后重新放到队列，让别人继续消费解锁
                //抛出异常，重新入队
                throw new RuntimeException("远程服务失败");
            }

        } else {
            //无需解锁
        }
    }

    /**
     * 解锁订单，防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期。查订单状态为新建，什么都不做就走了
     * 导致卡顿的订单，永远不能解锁库存
     * @param to
     */
    @Override
    @Transactional
    public void unLockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        //查一下库存最新解锁状态，防止重复解锁库存
        WareOrderTaskEntity task= orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = task.getId();
        //按照工作单找到所有没有解锁的库存，精心解锁
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskId)
                .eq("lock_status", 1)//状态为1表示新建
        );
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}