package cc.xiaowei.url_convert.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
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
public class ConvertCounter {

    private final static LongAdder longAdder = new LongAdder();
    private final RedisTemplate<String, Object> redisTemplate;

    @After("execution(* cc.xiaowei.url_convert.controller.ConvertController.convert(..))")
    public void pointCut() {
        try {
            longAdder.increment();
        } catch (Exception e) {
            log.error("increment fail", e);
        }
    }


    @Async
    @Scheduled(fixedDelay = 6000 * 5)
    public void update(){
        long sumThenReset = longAdder.sumThenReset();
        if (sumThenReset == 0) return;
        redisTemplate.opsForValue().increment("statistics:convert_count",sumThenReset);
        log.info("update convert count:{}", sumThenReset);
    }

}
