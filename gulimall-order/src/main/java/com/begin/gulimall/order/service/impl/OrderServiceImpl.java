package com.begin.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.begin.gulimall.common.exception.NoStockException;
import com.begin.gulimall.common.to.mq.OrderTo;
import com.begin.gulimall.common.to.mq.SeckillOrderTo;
import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.common.vo.MemberRespVo;
import com.begin.gulimall.order.constant.OrderConstant;
import com.begin.gulimall.order.dao.OrderItemDao;
import com.begin.gulimall.order.entity.OrderItemEntity;
import com.begin.gulimall.order.entity.PaymentInfoEntity;
import com.begin.gulimall.order.enume.OrderStatusEnum;
import com.begin.gulimall.order.feign.CartFeignService;
import com.begin.gulimall.order.feign.MemberFeignService;
import com.begin.gulimall.order.feign.ProductFeignService;
import com.begin.gulimall.order.feign.WmsFeignService;
import com.begin.gulimall.order.interceptor.LoginUserInterceptor;
import com.begin.gulimall.order.service.OrderItemService;
import com.begin.gulimall.order.service.PaymentInfoService;
import com.begin.gulimall.order.to.OrderCreateTo;
import com.begin.gulimall.order.vo.*;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.begin.gulimall.common.utils.PageUtils;
import com.begin.gulimall.common.utils.Query;

import com.begin.gulimall.order.dao.OrderDao;
import com.begin.gulimall.order.entity.OrderEntity;
import com.begin.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页需要的数据
     *
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.LoginUser.get();

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();


        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //保证多线程情况下请求上下文不丢失
            RequestContextHolder.setRequestAttributes(attributes);
            //1、远程查询所有的收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            //2、远程查询购物车所有选中的购物项
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());

            R hasStock = wmsFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> booleanMap = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(booleanMap);
            }
        }, executor);


        //3、获取用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4、订单总额\应付价格
        //自动计算

        //TODO 5、防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_TOKEN_ORDER_PREFIX + memberRespVo.getId(), token, 30L, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

    /**
     * 下单方法 //下单：创建订单、验价格、验临牌、锁库存....
     *
     * @param vo
     * @return
     * @GlobalTransactional Seata  AT分布式事务
     */

    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);

        //1、验证临牌【令牌的对比和删除必须保证原子性】
        //0-令牌失败 1-令牌成功
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        MemberRespVo memberRespVo = LoginUserInterceptor.LoginUser.get();
        String orderToken = vo.getOrderToken();

        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_TOKEN_ORDER_PREFIX + memberRespVo.getId());
        //使用lua脚本原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_TOKEN_ORDER_PREFIX + memberRespVo.getId()), orderToken);
        if (result == 0L) {
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //令牌验证成功
            //下单：去创建订单，验证令牌，验价格，锁库存
            //1、创建订单，订单项等信息
            OrderCreateTo order = createOrder();

            //2、验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //金额对比

                //3、保存订单
                saveOrder(order);
                //4、库存锁定,出现异常，全部回滚
                //订单号，所有订单项(skuId、skuName、num)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(orderItemVos);
                //远程锁定库存
                //库存成功了。但是网络原因超时，订单回滚，库存不回滚
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //锁定成功
                    responseVo.setOrderEntity(order.getOrder());

                    //TODO 远程扣减积分  模拟异常引出分布式事务
//                    int i = 10 / 0;//订单回滚，库存不回滚
                    //TODO 订单创建成功发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return responseVo;
                } else {
                    //TODO 锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
//                    responseVo.setCode(3);
//                    return responseVo;

                }

            } else {
                //验价失败
                responseVo.setCode(2);
                return responseVo;
            }
        }

    }

    /**
     * 远程返回订单状态
     *
     * @param orderSn
     * @return
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    /**
     * 关闭过期订单
     *
     * @param entity
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        //关单
        if (orderEntity.getStatus() == OrderStatusEnum.CRETAE_NEW.getCode()) {
            OrderEntity entity1 = new OrderEntity();
            entity1.setStatus(OrderStatusEnum.CANCLED.getCode());
            entity1.setId(entity.getId());
            this.updateById(entity1);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            //发给MQ一个，防止因为网络问题导致库存解锁失败
            try {
                //TODO 保证消息一定会发送出去，每一个消息都做好日志记录（给数据库保存每一个消息的详细信息）。
                //TODO 定期扫描数据库，将失败的消息重新发送
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (AmqpException e) {
                //TODO 将没发送成功的消息进行重试发送

            }
        }

    }

    /**
     * 获取当前订单支付信息
     *
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity entity = order_sn.get(0);

        payVo.setBody(entity.getSkuAttrsVals());            //订单的备注
        payVo.setOut_trade_no(orderEntity.getOrderSn());    //订单号
        payVo.setSubject(entity.getSkuName());         //订单的主题

        BigDecimal decimal = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);//保留两位小数向上取整
        payVo.setTotal_amount(decimal.toString());    //订单的金额
        return payVo;
    }

    /**
     * 完整列表信息
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo respVo = LoginUserInterceptor.LoginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", respVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntityList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntityList);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(order_sn);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝支付返回数据
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1、保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        //2、修改订单的状态信息
        if(vo.getTrade_status().equals("TRADE_SUCCESS")||vo.getTrade_status().equals("TRADE_FINISHED")){
            //支付成功状态
            String tradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(tradeNo,OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo entity) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(entity.getOrderSn());
        orderEntity.setMemberId(entity.getMemberId());
        //收货地址...
        orderEntity.setStatus(OrderStatusEnum.CRETAE_NEW.getCode());
        BigDecimal multiply = entity.getSeckillPrice().multiply(new BigDecimal("" + entity.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(entity.getOrderSn());
        itemEntity.setRealAmount(multiply);
        //TODO 获取当前sku的详细信息进行设置 productFeignService.getSpuInfoBySkuId()
        itemEntity.setSkuQuantity(entity.getNum());
        orderItemService.save(itemEntity);


    }

    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        //1、生成订单号
        String orderSn = IdWorker.getTimeId();
        //创建订单
        OrderEntity orderEntity = buildOrder(orderSn);

        //获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //3、验价
        computePrice(orderEntity, itemEntities);

        createTo.setOrder(orderEntity);
        createTo.setOrderItems(itemEntities);

        return createTo;
    }

    /**
     * 计算价格相关
     *
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {

        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        //订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity entity : itemEntities) {
            BigDecimal realAmount = entity.getRealAmount();
            total = total.add(realAmount);
            //优惠
            BigDecimal couponAmount = entity.getCouponAmount();
            coupon = coupon.add(couponAmount);
            //积分
            BigDecimal integrationAmount = entity.getIntegrationAmount();
            integration = integration.add(integrationAmount);
            //打折
            BigDecimal promotionAmount = entity.getPromotionAmount();
            promotion = promotion.add(promotionAmount);

            //积分和成长值
            gift = gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString()));
        }

        //1、订单价格相关
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        //设置积分等信息
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        //删除状态：未删除
        orderEntity.setDeleteStatus(0);


    }

    /**
     * 生成订单
     *
     * @param orderSn
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo respVo = LoginUserInterceptor.LoginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(respVo.getId());
        //获取收货地址信息
        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费
        entity.setFreightAmount(fareResp.getFare());
        //设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //设置订单状态信息
        entity.setStatus(OrderStatusEnum.CRETAE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * 构建所有订单项数据
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1、订单信息：订单号
        //2、商品的SPU信息
        Long skuId = cartItem.getSkuId();
        //
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());


        //3、商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");//按照指定分隔符将数组进行转换
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4、优惠信息（暂无）
        //5、积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        //6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的总金额
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getIntegrationAmount());
        //当前订单项的实际金额=总金额-各种优惠
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }

}