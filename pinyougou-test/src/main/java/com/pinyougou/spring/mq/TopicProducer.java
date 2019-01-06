package com.pinyougou.spring.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-05 18:57
 */
@Component
public class TopicProducer {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("topicTextDestination")
    private Destination destination;

    public void sendTextMessage(final String text){
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(text);
            }
        };
        jmsTemplate.send(destination,messageCreator);
    }
}
