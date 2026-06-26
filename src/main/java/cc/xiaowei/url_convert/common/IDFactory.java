package cc.xiaowei.url_convert.common;

import cc.xiaowei.url_convert.Application;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class IDFactory {

    private final static long START_STAMP_SEC_UTC = 1782345600L; //2026-06-25 00:00:00 UTC
    private final static byte INCREMENT_BITS = 32;
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    private final RedissonClient redissonClient;

    public Long next(){
        long timestamp_sec_utc = Instant.now().getEpochSecond();
        ZonedDateTime zonedDateTime = Instant.ofEpochSecond(timestamp_sec_utc).atZone(Application.ZONE_ID);
        String incrementKey = zonedDateTime.format(DATE_TIME_FORMATTER);
        long increment = redissonClient.getAtomicLong(incrementKey).incrementAndGet();
        return (timestamp_sec_utc - START_STAMP_SEC_UTC) << INCREMENT_BITS | increment;
    }

    public static long extractTimeStampSecUTC(Long id){
        return  (id >> INCREMENT_BITS) + START_STAMP_SEC_UTC;
    }

    public static YearMonth extractYearMonth(Long id){
        long timestamp_sec_utc = extractTimeStampSecUTC(id);
        return YearMonth.from(Instant.ofEpochSecond(timestamp_sec_utc).atZone(Application.ZONE_ID));
    }
}
