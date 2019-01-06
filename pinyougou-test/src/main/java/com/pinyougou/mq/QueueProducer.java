package com.pinyougou.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-04 22:55
 */
public class QueueProducer {
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
        //5、创建一个队列对象
        Queue queue = session.createQueue("test-queue");
        //6、创建一个消息的生产者对象
        MessageProducer producer = session.createProducer(queue);
        //7、创建消息对象(文本消息)
        TextMessage textMessage = session.createTextMessage("欢迎来到神奇的品优购世界1");
        //8、发送消息
        producer.send(textMessage);

        //9、关闭资源
        producer.close();
        session.close();
        connection.close();
    }
}
