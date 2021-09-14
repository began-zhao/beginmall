package com.begin.gulimall.seckill.controller;

import com.begin.gulimall.common.utils.R;
import com.begin.gulimall.seckill.scheduled.service.SeckillService;
import com.begin.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;



    /**
     * 返回当前时间可以参与的秒杀商品信息
     *
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    @CrossOrigin
    @ResponseBody
    public R getCurrentSeckillSkus() {

        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    /**
     * 获取某一个sku商品的秒杀信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);

        return R.ok().setData(to);
    }

    /**
     * 秒杀请求
     *
     * @return
     */
    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num,
                     Model model) {
        //1、拦截器判断是否登录
        String orderSn = seckillService.kill(killId, key, num);

        model.addAttribute("orderSn",orderSn);
//        return R.ok().setData(orderSn);
        return "success";
    }

}
