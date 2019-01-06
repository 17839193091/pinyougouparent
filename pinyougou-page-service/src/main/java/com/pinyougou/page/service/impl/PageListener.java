package com.pinyougou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.pinyougou.page.service.ItemPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * 描述: 监听类，用于生成网页
 *
 * @author hudongfei
 * @create 2019-01-06 0:44
 */
public class PageListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Page.class);
    @Resource
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] ids = (Long[]) objectMessage.getObject();
            LOGGER.info("PageListener-接收到消息:"+ JSON.toJSONString(ids));
            for (Long id : ids) {
                itemPageService.getItemHtml(id);
            }
        } catch (JMSException e) {
            LOGGER.error("PageListener-消费消息失败.",e);
        }

    }
}
