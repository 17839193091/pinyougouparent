package cn.itcast.sms;

import com.aliyuncs.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-06 21:43
 */
@Component
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;

    private Logger logger = LoggerFactory.getLogger(SmsListener.class);

    @JmsListener(destination = "sms")
    public void sendSms(Map<String,String> map){
        try {
            logger.info("接收到短信消息:"+map);
            smsUtil.sendSms(map.get("mobile"),map.get("template_code"),map.get("sign_name"),map.get("param"));
        } catch (ClientException e) {
            logger.error("SmsListener-短信消息消费失败",e);
        }
    }
}
