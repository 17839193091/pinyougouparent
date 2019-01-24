package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-15 21:51
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeiXinPayService weiXinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map<String, String> createNative() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
        if (seckillOrder != null) {
            return weiXinPayService.createNative(seckillOrder.getId()+"", seckillOrder.getMoney().doubleValue()*100+"");
        }
        return new HashMap<>();
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPageStatus(String out_trade_no) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int x = 0;
        while (true) {
            Map<String, String> map = weiXinPayService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false,"支付发生异常");
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))){
                //保存订单
                seckillOrderService.saveOrderFromRedisToDB(username,Long.valueOf(out_trade_no),map.get("transaction_id"));
                result = new Result(true,"支付成功");
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            x++;

            if (x >= 100) {
                result = new Result(false,"支付超时");
                //关闭微信支付订单
                Map<String, String> closePay = weiXinPayService.closePay(out_trade_no);
                //如果在关闭订单失败，并且是订单已支付的原因，则走正常订单支付成功的逻辑
                if ("FAIL".equals(closePay.get("return_code")) && "ORDERPAID".equals(closePay.get("err_code"))){
                    //保存订单
                    seckillOrderService.saveOrderFromRedisToDB(username,Long.valueOf(out_trade_no),map.get("transaction_id"));
                    result = new Result(true,"支付成功");
                }

                //删除秒杀订单
                if (!result.isSuccess()){
                    seckillOrderService.deleteOrderFromRedis(username,Long.valueOf(out_trade_no));
                }
                break;
            }
        }

        return result;
    }
}
