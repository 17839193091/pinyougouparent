package com.pinyougou.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-04 23:42
 */
public class TopicConsumer {
    public static void main(String[] args) throws JMSException, IOException {
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

        MessageConsumer consumer = session.createConsumer(topic);

        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println(textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });


        System.in.read();

        consumer.close();
        session.close();
        connection.close();
    }
}
