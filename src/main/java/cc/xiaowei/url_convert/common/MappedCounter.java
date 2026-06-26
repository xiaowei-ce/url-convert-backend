package cc.xiaowei.url_convert.common;

import cc.xiaowei.url_convert.configs.RedisConsts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class MappedCounter {

    private final static LongAdder longAdder = new LongAdder();
    private final RedisTemplate<String, Object> redisTemplate;

    @AfterReturning("execution(* cc.xiaowei.url_convert.controller.MapURLController.mapURL(..))")
    public void pointCut() {
        try {
            longAdder.increment();
        } catch (Exception e) {
            log.error("increment fail", e);
        }
    }


    @Async
    @Scheduled(fixedDelay = 10000 * 30)
    public void update(){
        long localCount = longAdder.sumThenReset();
        if (localCount == 0) return;
        redisTemplate.opsForValue().increment(RedisConsts.STATISTICS_MAPPED_COUNT_KEY, localCount);
        log.info("update convert count:{}", localCount);
    }

}
