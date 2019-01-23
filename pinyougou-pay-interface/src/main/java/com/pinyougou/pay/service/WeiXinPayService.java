package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-15 21:24
 */
public interface WeiXinPayService {

    /**
     * 生成二维码所需的url
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    Map<String,String> createNative(String out_trade_no,String total_fee);

    /**
     * 查询支付订单状态
     * @param out_trade_no
     * @return
     */
    Map<String,String> queryPayStatus(String out_trade_no);
}
