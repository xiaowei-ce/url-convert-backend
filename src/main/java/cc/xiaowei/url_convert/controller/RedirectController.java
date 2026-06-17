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
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Duration;
import java.util.Objects;
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


    private final Cache<String, String> localCache = CacheBuilder.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .initialCapacity(200)
            .build();

    private final RedirectView NOT_FOUND = new RedirectView("http://localhost:5173/not-found.html");

    private final RedirectView RETRY = new RedirectView("http://localhost:5173/retry");

    @GetMapping("/{uri}")
    public RedirectView redirect(@PathVariable String uri) {

        Long id = Longs.tryParse(Convertor.revert(uri));
        if (id == null) {
            return NOT_FOUND;
        }
        String cachedKey = "uri_id:" + id;

        //try local & redis cache
        RedirectView firstTryGet = buildRedirectFrom2CacheOrNull(cachedKey);
        if (firstTryGet != null){
            return firstTryGet;
        }

        //update local & redis cache with locked if not hit
        RLock lock = redisson.getLock(cachedKey);
        try {
            if (!lock.tryLock()) {
                TimeUnit.MILLISECONDS.sleep(100); // just wait for cache, not lock
                RedirectView otherThreadMayRecachedInMySleepTime = buildRedirectFrom2CacheOrNull(cachedKey);
                return Objects.requireNonNullElse(otherThreadMayRecachedInMySleepTime, RETRY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        try {
            //double check redis & local if got lock
            RedirectView aReadyGotLockButCheckAgainCauseOtherThreadMayRecached = buildRedirectFrom2CacheOrNull(cachedKey);
            if (aReadyGotLockButCheckAgainCauseOtherThreadMayRecached != null){
                return aReadyGotLockButCheckAgainCauseOtherThreadMayRecached;
            }

            Converted selected = convertedMapper.selectById(id);
            if (selected == null) {
                redisTemplate.opsForValue().set(cachedKey, "", Duration.ofMinutes(15 + randomInt(-5, 15)));
                localCache.put(cachedKey, "");
                return NOT_FOUND;
            }

            //recache redis & local
            redisTemplate.opsForValue().set(cachedKey, selected.getOriginal(), Duration.ofMinutes(360 + randomInt(-20, 45)));
            localCache.put(cachedKey, selected.getOriginal());
            return new RedirectView(selected.getOriginal());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }


    private @Nullable RedirectView buildRedirectFrom2CacheOrNull(@NonNull String cachedKey){

        //try local
        String localCached = localCache.getIfPresent(cachedKey);
        if (localCached != null) {
            if (localCached.isEmpty()) {
                return NOT_FOUND;
            }
            return new RedirectView(localCached);
        }

        //try redis
        String redisCached = Cast.cast(redisTemplate.opsForValue().get(cachedKey), String.class);
        if (redisCached != null) {
            if (redisCached.isEmpty()) {
                localCache.put(cachedKey, "");
                return NOT_FOUND;
            }
            localCache.put(cachedKey, redisCached);
            return new RedirectView(redisCached);
        }

        return null;
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

    private int randomInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

}
