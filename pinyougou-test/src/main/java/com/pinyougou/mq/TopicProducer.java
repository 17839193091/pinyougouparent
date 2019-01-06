package com.pinyougou.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-04 23:36
 */
public class TopicProducer {
    public static void main(String[] args) throws JMSException {
        //1、创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.128:61616");
        //2、创建连接
        Connection connection = connectionFactory.createConnection();
        //3、启动连接
        connection.start();
        //4、获取session（会话）
        // 参数1：是否启用事物   参数2：消息的确认方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("test-topic");
        MessageProducer producer = session.createProducer(topic);
        Message message = session.createTextMessage("欢迎来到神奇的品有够世界");
        producer.send(message);
        producer.close();
        session.close();
        connection.close();
    }
}
