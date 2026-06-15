package cc.xiaowei.url_convert.common;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RedisId {


    private final static long START_STAMP = 1780911857L; //sec
    private final static int BITS = 32;

    private final RedissonClient redissonClient;

    public Long get(){
        LocalDateTime nowMinute = LocalDateTime.now(ZoneOffset.UTC).withSecond(0).withNano(0);
        String formattedToday = nowMinute.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        RAtomicLong atomicLong = redissonClient.getAtomicLong(formattedToday);
        long increment  = atomicLong.getAndIncrement();
        return (nowMinute.toEpochSecond(ZoneOffset.UTC) - START_STAMP) << BITS | increment;
    }
}
