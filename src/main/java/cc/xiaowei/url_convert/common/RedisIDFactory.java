package cc.xiaowei.url_convert.common;

import cc.xiaowei.url_convert.Application;
import cc.xiaowei.url_convert.configs.RedisConsts;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@RequiredArgsConstructor
public class RedisIDFactory extends IDFactory {


    private final RedissonClient redissonClient;

    @Override
    public Long next(){
        long timestamp_sec_utc = Instant.now().getEpochSecond();
        ZonedDateTime zonedDateTime = Instant.ofEpochSecond(timestamp_sec_utc).atZone(Application.ZONE_ID);
        String incrementKey = RedisConsts.ID_INCREMENT_KEY_PREFIX + zonedDateTime.format(DATE_TIME_FORMATTER);
        long increment = redissonClient.getAtomicLong(incrementKey).incrementAndGet();

        return ((timestamp_sec_utc - START_STAMP_SEC_UTC) / 60) << INCREMENT_BITS | increment;
    }


}
