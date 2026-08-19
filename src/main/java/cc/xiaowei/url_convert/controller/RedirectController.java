package cc.xiaowei.url_convert.controller;

import cc.xiaowei.url_convert.Application;
import cc.xiaowei.url_convert.common.Cast;
import cc.xiaowei.url_convert.common.FrontPagesURL;
import cc.xiaowei.url_convert.common.IDFactory;
import cc.xiaowei.url_convert.common.IdUriConvert;
import cc.xiaowei.url_convert.configs.RedisConsts;
import cc.xiaowei.url_convert.configs.rabbitmq.RabbitConsts;
import cc.xiaowei.url_convert.entity.Result;
import cc.xiaowei.url_convert.entity.URLMap;
import cc.xiaowei.url_convert.exception.BizException;
import cc.xiaowei.url_convert.exception.NotFoundException;
import cc.xiaowei.url_convert.exception.RedirectException;
import cc.xiaowei.url_convert.mapper.URLMapMapper;
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
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@RestController
@Slf4j
public class RedirectController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redisson;

    private static final Cache<String, String> localCache = CacheBuilder.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(15, TimeUnit.SECONDS)
            .initialCapacity(200)
            .build();

    private final URLMapMapper uRLMapMapper;


    @GetMapping("/{uri}")
    public RedirectView redirect(@PathVariable String uri) {
        Long id = IdUriConvert.uri2IdElseNull(uri);

        if (id == null || YearMonth.now(Application.ZONE_ID).isBefore(IDFactory.extractYearMonth(id))) {
            NotFoundException._throw();
        }

        String cachedKey = RedisConsts.URLMAP_CACHE_KEY_REFIX + id;

        //try local & redis cache
        RedirectView firstTryGet = buildRedirectFromCacheElseNull(cachedKey);
        if (firstTryGet != null) {
            return firstTryGet;
        }

        //update local & redis cache with locked if not hit
        String lockKey = "" + id;
        RLock lock = redisson.getLock(lockKey);

        //lock fail
        if (!lock.tryLock()) {
            try {
                TimeUnit.MILLISECONDS.sleep(100); // just wait for cache, not lock
            } catch (InterruptedException e) {
                RedirectException.throw_(e.getMessage());
            }

            //other thread maybe recached in sleep time
            RedirectView checkAfterSleep = buildRedirectFromCacheElseNull(cachedKey);
            return Objects.requireNonNullElse(checkAfterSleep,
                    buildRedirect(FrontPagesURL.RETRY_URL,Map.of("uri",uri))); //retry if null
        }

        //lock success
        try {
            //other thread maybe recached
            RedirectView doubleCheck = buildRedirectFromCacheElseNull(cachedKey);
            if (doubleCheck != null) {
                return doubleCheck;
            }

            URLMap mapped = uRLMapMapper.selectById(id);
            if (mapped == null) {
                redisTemplate.opsForValue().set(cachedKey, "", Duration.ofMinutes(15 + randomInt(-5, 15)));
                localCache.put(cachedKey, "");
                NotFoundException._throw();
            }

            //recache redis & local
            redisTemplate.opsForValue().set(cachedKey, mapped.getUrl(), Duration.ofMinutes(360 + randomInt(-20, 45)));
            localCache.put(cachedKey, mapped.getUrl());
            return new RedirectView(mapped.getUrl());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }


    private @Nullable RedirectView buildRedirectFromCacheElseNull(@NonNull String cachedKey) {

        //try local
        String localCached = localCache.getIfPresent(cachedKey);
        if (localCached != null) {
            if (localCached.isEmpty()) {
                NotFoundException._throw();
            }
            return new RedirectView(localCached);
        }

        //try redis
        String redisCached = Cast.cast(redisTemplate.opsForValue().get(cachedKey), String.class);
        if (redisCached != null) {
            if (redisCached.isEmpty()) {
                localCache.put(cachedKey, "");
                NotFoundException._throw();
            }
            localCache.put(cachedKey, redisCached);
            return new RedirectView(redisCached);
        }

        return null;
    }


    @DeleteMapping("/del/{uri}")
    public Result<?> delete(@PathVariable String uri) {
        Long id = IdUriConvert.uri2IdElseNull(uri);
        if (id == null) {
            BizException.throw_("incorrect short link");
        }

        String key = RedisConsts.URLMAP_CACHE_KEY_REFIX + id;
        redisTemplate.delete(key);
        rabbitTemplate.convertAndSend(RabbitConsts.URLMAP.TOPIC_EXCHANGE, RabbitConsts.URLMAP.DEL_ROUTING_KEY, id);
        return Result.success(null);
    }

    private int randomInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    private RedirectView buildRedirect(String url, Map<String, ?> attrs) {
        RedirectView redirectView = new RedirectView(url);
        if (!attrs.isEmpty()) {
            redirectView.setAttributesMap(attrs);
        }
        return redirectView;
    }

    public static void deleteLocalCached(String key) {
        localCache.invalidate(key);
    }

}
