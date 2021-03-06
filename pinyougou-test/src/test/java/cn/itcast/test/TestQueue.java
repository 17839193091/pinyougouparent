package cn.itcast.test;

import com.pinyougou.spring.mq.QueueProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-05 16:00
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/*")
public class TestQueue {
    @Autowired
    private QueueProducer queueProducer;

    @Autowired
    private Environment environment;

    @Test
    public void test() {
        //queueProducer.sendTextMessage("hello MQ111");
    }
}
