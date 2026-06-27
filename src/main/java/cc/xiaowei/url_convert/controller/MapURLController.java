package cc.xiaowei.url_convert.controller;

import cc.xiaowei.url_convert.common.Cast;
import cc.xiaowei.url_convert.common.IdBase62Convertor;
import cc.xiaowei.url_convert.common.IDFactory;
import cc.xiaowei.url_convert.configs.RedisConsts;
import cc.xiaowei.url_convert.configs.rabbitmq.RabbitConsts;
import cc.xiaowei.url_convert.entity.Result;
import cc.xiaowei.url_convert.entity.URLMap;
import cc.xiaowei.url_convert.exception.BizException;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
@RequestMapping("/convert")
public class MapURLController {

    private static final String SERVER_DOMAIN = "http://localhost:8080";

    private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);
    private final IDFactory IDFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;


    @PostMapping
    public Result<String> mapURL(@RequestParam String url) {

        if (!urlValidator.isValid(url)) {
            BizException.throw_("Url is invalid");
        }
        Long id = IDFactory.next();

        String mappedUri = IdBase62Convertor.idTobase62str(id);
        if (Strings.isNullOrEmpty(mappedUri)) {
            BizException.throw_("URL map failed");
        }
        String converted_url = String.format("%s/%s", SERVER_DOMAIN, mappedUri);
        redisTemplate.opsForValue().set(RedisConsts.MAPPED_REDIS_KEY_REFIX + id, url,12 , TimeUnit.HOURS);

        URLMap mapped = new URLMap();
        mapped.setId(id);
        mapped.setUrl(url);
        rabbitTemplate.convertAndSend(RabbitConsts.TOPIC_EXCHANGE, RabbitConsts.MAPPED_ROUTING_KEY, mapped);

        return Result.success(converted_url);
    }

    @GetMapping("/statistics")
    public Result<Long> statistics() {
        Number count = Cast.cast(redisTemplate.opsForValue().get(RedisConsts.STATISTICS_MAPPED_COUNT_KEY), Number.class);
        if (count == null) {
            return Result.success(0L);
        }
        return Result.success(count.longValue());
    }

}
