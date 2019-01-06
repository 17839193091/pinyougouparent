package com.pinyougou.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-04 23:21
 */
public class QueueConsumer {
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
        //5、创建一个队列对象
        Queue queue = session.createQueue("test-queue");
        //6、创建一个消息的消费者对象
        MessageConsumer consumer = session.createConsumer(queue);
        //7、设置监听
        MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("提取的消息:"+textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        };
        consumer.setMessageListener(messageListener);

        //8、等待键盘输入
        System.in.read();

        //9、关闭资源
        consumer.close();
        session.close();
        connection.close();
    }
}
