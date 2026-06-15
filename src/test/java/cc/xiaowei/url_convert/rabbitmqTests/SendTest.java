package cc.xiaowei.url_convert.rabbitmqTests;

import cc.xiaowei.url_convert.configs.rabbitmq.consts;
import cc.xiaowei.url_convert.entity.Converted;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SendTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void send(){
        Converted converted = new Converted();
        converted.setOriginal("https://baidu.com");
        converted.setDomain("test.com");
        converted.setId(162671886836003L);
        converted.setDeleted(false);

        rabbitTemplate.convertAndSend(consts.TOPIC_EXCHANGE, consts.CONVERTED_ROUTING_KEY, converted);
    }

    @Test
    public void test(){
        rabbitTemplate.convertAndSend(consts.TOPIC_EXCHANGE,"back_to_me","test msg");
    }
}
