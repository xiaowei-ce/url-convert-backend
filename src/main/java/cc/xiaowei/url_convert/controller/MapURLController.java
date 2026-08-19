package cc.xiaowei.url_convert.controller;

import cc.xiaowei.url_convert.common.CachedIDFactory;
import cc.xiaowei.url_convert.common.Cast;
import cc.xiaowei.url_convert.common.FrontPagesURL;
import cc.xiaowei.url_convert.common.IdUriConvert;
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


    private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final CachedIDFactory cachedIDFactory;


    @PostMapping
    public Result<String> mapURL(@RequestParam String url) {

        if (!urlValidator.isValid(url)) {
            BizException.throw_("Url is invalid");
        }
        Long id = cachedIDFactory.next();

        String mappedUri = IdUriConvert.id2Uri(id);
        if (Strings.isNullOrEmpty(mappedUri)) {
            BizException.throw_("URL map failed");
        }
        String converted_url = FrontPagesURL.REDIRECT_BASE_URL + mappedUri;
        redisTemplate.opsForValue().set(RedisConsts.URLMAP_CACHE_KEY_REFIX + id, url,12 , TimeUnit.HOURS);

        URLMap mapped = new URLMap();
        mapped.setId(id);
        mapped.setUrl(url);
        rabbitTemplate.convertAndSend(RabbitConsts.URLMAP.TOPIC_EXCHANGE, RabbitConsts.URLMAP.FINISHED_ROUTING_KEY, mapped);

        return Result.success(converted_url);
    }

    @GetMapping("/statistics")
    public Result<Long> statistics() {
        Number count = Cast.cast(redisTemplate.opsForValue().get(RedisConsts.STATISTICS_URLMAP_OK_COUNT_KEY), Number.class);
        if (count == null) {
            return Result.success(0L);
        }
        return Result.success(count.longValue());
    }

}
