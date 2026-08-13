package cc.xiaowei.url_convert.common;

import cc.xiaowei.url_convert.Application;

import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;


    /*

        |     32bits     |     32bits     |
        |   分钟级时间戳   |     自增数      |

    */



    public abstract class IDFactory {

    static long START_STAMP_SEC_UTC = 1782345600L; //2026-06-25 00:00:00 UTC
    static byte INCREMENT_BITS = 32;
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

   abstract Long next();


   public static long extractTimeStampSecUTC(long id) {
        return (id >>> INCREMENT_BITS) * 60 + START_STAMP_SEC_UTC;
    }

    public static YearMonth extractYearMonth(long id) {
        long timestamp_sec_utc = extractTimeStampSecUTC(id);
        return YearMonth.from(Instant.ofEpochSecond(timestamp_sec_utc).atZone(Application.ZONE_ID));
    }

    public static long extractIncrement(long id) {
        return id & 0xFFFFFFFFL;
    }


}
