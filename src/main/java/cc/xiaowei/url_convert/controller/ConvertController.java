package cc.xiaowei.url_convert.controller;

import cc.xiaowei.url_convert.common.Cast;
import cc.xiaowei.url_convert.common.Convertor;
import cc.xiaowei.url_convert.common.RedisId;
import cc.xiaowei.url_convert.configs.rabbitmq.consts;
import cc.xiaowei.url_convert.entity.Converted;
import cc.xiaowei.url_convert.entity.Result;
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
public class ConvertController {

    private static final String SERVER_DOMAIN = "http://localhost:8080";

    private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);
    private final RedisId redisId;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;


    @PostMapping
    public Result<String> convert(@RequestParam String url) {

        if (!urlValidator.isValid(url)) {
            BizException.throw_("Url is invalid");
        }
        Long id = redisId.get();

        String uri = Convertor.convert(id);
        if (Strings.isNullOrEmpty(uri)) {
            BizException.throw_("convert failed");
        }
        String converted_url = String.format("%s/%s", SERVER_DOMAIN, uri);
        redisTemplate.opsForValue().set("uri_id:" + id, url,12 , TimeUnit.HOURS);

        Converted converted = new Converted();
        converted.setDomain(SERVER_DOMAIN);
        converted.setId(id);
        converted.setOriginal(url);
        rabbitTemplate.convertAndSend(consts.TOPIC_EXCHANGE, consts.CONVERTED_ROUTING_KEY, converted);

        return Result.success(converted_url);
    }

    @GetMapping("/statistics")
    public Result<Long> statistics() {
        Number count = Cast.cast(redisTemplate.opsForValue().get("statistics:convert_count"), Number.class);
        if (count == null) {
            return Result.success(0L);
        }
        return Result.success(count.longValue());
    }

}
