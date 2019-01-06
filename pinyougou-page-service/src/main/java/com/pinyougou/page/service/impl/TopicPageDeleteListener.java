package com.pinyougou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-06 9:41
 */
public class TopicPageDeleteListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicPageDeleteListener.class);
    @Resource
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;

        try {
            Long[] ids = (Long[]) objectMessage.getObject();
            LOGGER.info("TopicPageDeleteListener-接收到消息:"+ JSON.toJSONString(ids));
            if (!itemPageService.deleteItemHtml(ids)){
                throw new RuntimeException("删除静态网页失败,日志已记录");
            }
        } catch (JMSException e) {
            LOGGER.error("TopicPageDeleteListener-消息消费失败.",e);
        }

    }
}
