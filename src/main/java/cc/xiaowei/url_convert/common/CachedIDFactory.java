package cc.xiaowei.url_convert.common;

import cc.xiaowei.url_convert.Application;
import cc.xiaowei.url_convert.configs.RedisConsts;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@RequiredArgsConstructor
public class CachedIDFactory extends IDFactory {

    private final RedissonClient redissonClient;
    private long offset = 0, end = 0; //[offset , end)
    private final long STEP = 200L;
    private LocalDate lastCache;

    @Override
    public synchronized Long next() {
        Instant instant = Instant.now();
        LocalDate today = instant.atZone(Application.ZONE_ID).toLocalDate();
        //跨天直接丢弃
        if (lastCache != null && !today.isEqual(lastCache)) {
            offset = 0;
            end = 0;
        }
        if (offset >= end) {
            String incrementKey = RedisConsts.ID_INCREMENT_KEY_PREFIX + today.format(DATE_TIME_FORMATTER);
            end = redissonClient.getAtomicLong(incrementKey).addAndGet(STEP);
            offset = end - STEP;
            lastCache = today;
        }

        long nowMins_utc = (instant.getEpochSecond() - START_STAMP_SEC_UTC) / 60;
        return nowMins_utc << INCREMENT_BITS | offset++;
    }
}
