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
    private long start = 0, end = 0;
    private final long STEP = 1500L;
    private LocalDate lastUseLocalDate;

    @Override
    public synchronized Long next() {

        long timestamp_sec_utc = Instant.now().getEpochSecond();
        LocalDate todayLocalDate = Instant.ofEpochSecond(timestamp_sec_utc).atZone(Application.ZONE_ID).toLocalDate();

        if (lastUseLocalDate != null && !todayLocalDate.isEqual(lastUseLocalDate)) { //跨天直接丢弃
            start = 0;
            end = 0;
        }
        if (start >= end) {
            String incrementKey = RedisConsts.ID_INCREMENT_KEY_PREFIX + todayLocalDate.format(DATE_TIME_FORMATTER);
            end = redissonClient.getAtomicLong(incrementKey).addAndGet(STEP);
            start = end - STEP;
        }
        lastUseLocalDate = todayLocalDate;
        return ((timestamp_sec_utc - START_STAMP_SEC_UTC) / 60) << INCREMENT_BITS | start++;
    }
}
