package cc.xiaowei.url_convert.rabbitmqTests;

import cc.xiaowei.url_convert.configs.rabbitmq.RabbitConsts;
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

    }

    @Test
    public void test(){
        rabbitTemplate.convertAndSend(RabbitConsts.TOPIC_EXCHANGE,"back_to_me","test msg");
    }
}
