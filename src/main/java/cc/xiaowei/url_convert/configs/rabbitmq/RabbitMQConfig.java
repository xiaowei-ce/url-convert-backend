package cc.xiaowei.url_convert.configs.rabbitmq;

import cc.xiaowei.url_convert.configs.RedisConsts;
import cc.xiaowei.url_convert.entity.URLMap;
import cc.xiaowei.url_convert.mapper.URLMapMapper;
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

    private final RedisTemplate<String, Object> redisTemplate;
    private final URLMapMapper uRLMapMapper;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = RabbitConsts.CONVERTED_QUEUE, durable = "true"),
                    exchange = @Exchange(name = RabbitConsts.TOPIC_EXCHANGE, durable = "true"),
                    key = {RabbitConsts.CONVERTED_ROUTING_KEY}
            ),
            concurrency = "3"
    )
    public  void converted(URLMap mapped) {
        log.info("received: {}",mapped);
        uRLMapMapper.insert(mapped);
    }


    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = RabbitConsts.DELETE_QUEUE, durable = "true"),
                    exchange = @Exchange(name = RabbitConsts.TOPIC_EXCHANGE, durable = "true"),
                    key = {RabbitConsts.DELETE_ROUTING_KEY}
            ),
            concurrency = "3"
    )
    public void delete(Long id) {
        log.info("delete {}",id);
        redisTemplate.delete(RedisConsts.MAPPED_REDIS_KEY_REFIX + id);
        uRLMapMapper.deleteById(id);
    }


    @Bean
    public MessageConverter  messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
