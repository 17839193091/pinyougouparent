package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbPayLog;
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
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map<String, String> createNative() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = orderService.searchPayLogFromRedis(username);
        if (tbPayLog != null) {
            return weiXinPayService.createNative(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee()+"");
        }
        return new HashMap<>();
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPageStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true) {
            Map<String, String> map = weiXinPayService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false,"支付发生异常");
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))){
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
                result = new Result(true,"支付成功");
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            x++;

            if (x >= 10) {
                result = new Result(false,"支付超时");
                break;
            }
        }

        return result;
    }
}
