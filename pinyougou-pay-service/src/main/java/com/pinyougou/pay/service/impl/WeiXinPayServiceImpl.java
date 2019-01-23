package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeiXinPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-15 21:30
 */
@Service(timeout = 10000)
public class WeiXinPayServiceImpl implements WeiXinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${notifyurl}")
    private String notifyurl;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeiXinPayServiceImpl.class);

    /**
     * 生成二维码所需的url
     *
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map<String, String> createNative(String out_trade_no, String total_fee) {
        //1、参数封装
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body","品优购-hdf");
        param.put("out_trade_no",out_trade_no);
        param.put("total_fee",total_fee);
        param.put("spbill_create_ip","127.0.0.1");
        param.put("notify_url",notifyurl);
        param.put("trade_type","NATIVE");

        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            LOGGER.info("生成请求参数字符串:"+paramXml);
            //2、发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3、获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            LOGGER.info("微信支付下单返回的结果:"+mapResult);
            Map<String,String> map = new HashMap<>();
            //map.put("code_url",mapResult.get("code_url"));
            map.put("code_url","http://www.baidu.com");
            map.put("out_trade_no",out_trade_no);
            map.put("total_fee",total_fee);
            return map;
        } catch (Exception e) {
            LOGGER.error("生成签名请求参数异常",e);
            return new HashMap<>();
        }

    }

    /**
     * 查询支付订单状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("out_trade_no",out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            //2、发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //3、获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            LOGGER.info("微信支付订单状态查询:"+mapResult);
            Map<String,String> map = new HashMap<>();
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
