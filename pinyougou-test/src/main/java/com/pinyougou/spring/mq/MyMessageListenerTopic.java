package com.pinyougou.spring.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-05 19:01
 */
public class MyMessageListenerTopic implements MessageListener {
    @Override
    public void onMessage(Message message) {
        TextMessage message1 = (TextMessage) message;
        try {
            System.out.println("接收到消息:"+message1.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
