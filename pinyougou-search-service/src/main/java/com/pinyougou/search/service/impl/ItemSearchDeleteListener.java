package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.search.service.ItemSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-05 23:50
 */
public class ItemSearchDeleteListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemSearchDeleteListener.class);

    @Resource
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            LOGGER.info("ItemSearchDeleteListener-接收到消息:"+ JSON.toJSONString(goodsIds));
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
        } catch (JMSException e) {
            LOGGER.error("ItemSearchDeleteListener-消费消息失败.",e);
        }
    }
}
