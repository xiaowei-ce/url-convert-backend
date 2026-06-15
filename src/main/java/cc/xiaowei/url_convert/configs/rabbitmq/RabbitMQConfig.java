package cc.xiaowei.url_convert.configs.rabbitmq;

import cc.xiaowei.url_convert.entity.Converted;
import cc.xiaowei.url_convert.mapper.ConvertedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final ConvertedMapper convertedMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = consts.CONVERTED_QUEUE, durable = "true"),
                    exchange = @Exchange(name = consts.TOPIC_EXCHANGE, durable = "true"),
                    key = {consts.CONVERTED_ROUTING_KEY}
            )
    )
    public  void converted(Converted converted) {
        log.info("received: {}",converted);
        convertedMapper.insert(converted);
    }


    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = consts.DELETE_QUEUE, durable = "true"),
                    exchange = @Exchange(name = consts.TOPIC_EXCHANGE, durable = "true"),
                    key = {consts.DELETE_ROUTING_KEY}
            )
    )
    public void delete(Long id) {
        log.info("delete {}",id);
        redisTemplate.delete("uri_id:" + id);
        convertedMapper.deleteById(id);
    }


    @Bean
    public MessageConverter  messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
