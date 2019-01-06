package com.pinyougou.search.service.impl;


import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-05 19:43
 */
public class ItemSearchListener implements MessageListener {

    @Resource
    private ItemSearchService itemSearchService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemSearchListener.class);
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            LOGGER.info("ItemSearchListener-监听到消息:"+text);
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            itemSearchService.importList(itemList);
        } catch (JMSException e) {
            LOGGER.error("MQ消息消费失败.",e);
        }
    }
}
