package cn.itcast.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-05 19:02
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/*")
public class TestTopicConsumer {
    @Test
    public void test() throws IOException {
        //System.in.read();
    }
}
