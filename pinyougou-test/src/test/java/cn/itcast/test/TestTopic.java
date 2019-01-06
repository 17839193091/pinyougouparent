package cn.itcast.test;

import com.pinyougou.spring.mq.TopicProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-05 18:57
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/*")
public class TestTopic {
    @Autowired
    private TopicProducer topicProducer;

    @Test
    public void test(){
        topicProducer.sendTextMessage("hello MQ111");
    }
}
