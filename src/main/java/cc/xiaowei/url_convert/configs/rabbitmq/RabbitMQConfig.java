package cc.xiaowei.url_convert.configs.rabbitmq;

import cc.xiaowei.url_convert.configs.RedisConsts;
import cc.xiaowei.url_convert.controller.RedirectController;
import cc.xiaowei.url_convert.entity.URLMap;
import cc.xiaowei.url_convert.mapper.URLMapMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RedisTemplate<String, Object> redisTemplate;
    private final URLMapMapper uRLMapMapper;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "urlmap.map_finished", durable = "true"),
                    exchange = @Exchange(name = RabbitConsts.URLMAP.TOPIC_EXCHANGE, durable = "true",type = ExchangeTypes.TOPIC),
                    key = {RabbitConsts.URLMAP.FINISHED_ROUTING_KEY}
            ),
            concurrency = "1"
    )
    public  void urlmapInsert(URLMap mapped) {
        log.info("received: {}",mapped);
        uRLMapMapper.insert(mapped);
    }


    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "urlmap.del.redis_db", durable = "true"),
                    exchange = @Exchange(name = RabbitConsts.URLMAP.TOPIC_EXCHANGE, durable = "true", type = ExchangeTypes.TOPIC),
                    key = RabbitConsts.URLMAP.DEL_ROUTING_KEY
            ),
            concurrency = "1"
    )
    public void urlMapDelete(Long id) {
        log.info("delete {}",id);
        uRLMapMapper.deleteById(id);
        redisTemplate.delete(RedisConsts.URLMAP_CACHE_KEY_REFIX + id);
    }



    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "urlmap.del.localcache." + "${server-no}", durable = "true"),
                    exchange = @Exchange(name = RabbitConsts.URLMAP.TOPIC_EXCHANGE, durable = "true", type = ExchangeTypes.TOPIC),
                    key = RabbitConsts.URLMAP.DEL_ROUTING_KEY
            )
    )
    public void deleteLocalCache(Long id){
        String key = RedisConsts.URLMAP_CACHE_KEY_REFIX + id;
        log.info("delete localcached key:{}",key);
        RedirectController.deleteLocalCached(key);
    }


    @Bean
    public MessageConverter  messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
