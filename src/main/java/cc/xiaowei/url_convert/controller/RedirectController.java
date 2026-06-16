package cc.xiaowei.url_convert.controller;

import cc.xiaowei.url_convert.common.Cast;
import cc.xiaowei.url_convert.common.Convertor;
import cc.xiaowei.url_convert.configs.rabbitmq.consts;
import cc.xiaowei.url_convert.entity.Converted;
import cc.xiaowei.url_convert.entity.Result;
import cc.xiaowei.url_convert.exception.BizException;
import cc.xiaowei.url_convert.mapper.ConvertedMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Longs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.N;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@RestController
@Slf4j
public class RedirectController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConvertedMapper convertedMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redisson;

    private final Cache<Long, String> localCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .initialCapacity(200)
            .recordStats()
            .build();

    private final RedirectView NOT_FOUND = new RedirectView("http://localhost:5173/not-found.html");

    @GetMapping("/{uri}")
    public RedirectView redirect(@PathVariable String uri) {

        Long id = Longs.tryParse(Convertor.revert(uri));
        if (id == null) {
            return  NOT_FOUND;
        }

        String url = Cast.cast(redisTemplate.opsForValue().get("uri_id:" + id), String.class);
        if (url == null) {
            RLock lock = redisson.getLock(String.valueOf(id));
            boolean locked = false;
            try {

                for (int i = 0; i < 3 && !locked; i++) {
                    if (!(locked = lock.tryLock(15, TimeUnit.SECONDS))) {
                        TimeUnit.MILLISECONDS.sleep(150);
                    }
                }
                if (!locked) {
                    BizException.throw_("please try again later");
                }

                Converted selected = convertedMapper.selectById(id);
                if (selected == null) {
                    redisTemplate.opsForValue().set("uri_id:" + id, "", 24 * 60 + ThreadLocalRandom.current().nextInt(-6 * 60,6 * 60), TimeUnit.MINUTES);
                    return NOT_FOUND;
                } else {
                    redisTemplate.opsForValue().set("uri_id:" + id, selected.getOriginal(), 24 * 60 + ThreadLocalRandom.current().nextInt(-6 * 60,6 * 60), TimeUnit.HOURS);
                    url = selected.getOriginal();
                }

            } catch (InterruptedException e) {
               BizException.throw_("service Interrupted");
            } finally {
                if (locked) {
                    lock.unlock();
                }
            }

        }
        if (url.isEmpty()) {
            return  NOT_FOUND;
        }

        return new RedirectView(url);
    }


    @DeleteMapping("/del/{uri}")
    public Result<String> delete(@PathVariable String uri) {
        Long id = Longs.tryParse(Convertor.revert(uri));
        if (id == null) {
            BizException.throw_("Incorrect short link because parsing failed");
        }
        redisTemplate.delete("uri_id:" + id);
        rabbitTemplate.convertAndSend(consts.TOPIC_EXCHANGE, consts.DELETE_ROUTING_KEY, id);
        return Result.success(null);
    }
}
