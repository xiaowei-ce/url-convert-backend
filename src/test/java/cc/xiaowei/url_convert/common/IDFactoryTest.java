package cc.xiaowei.url_convert.common;

import cc.xiaowei.url_convert.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;

@SpringBootTest
public class IDFactoryTest {

    @Autowired
    IDFactory IDFactory;

    @Test
    public void nextTest(){
//        for (int i = 0; i < 5000; i++) {
//            redisId.get();
//        }
//        System.out.println(IDFactory.next());
        System.out.println(cc.xiaowei.url_convert.common.IDFactory.extractYearMonth(27401891348481L));
        System.out.println(cc.xiaowei.url_convert.common.IDFactory.extractIncrement(27401891348481L));
        System.out.println(cc.xiaowei.url_convert.common.IDFactory.extractTimeStampSecUTC(27401891348481L));
        System.out.println(Instant.ofEpochSecond(cc.xiaowei.url_convert.common.IDFactory.extractTimeStampSecUTC(27401891348481L)).atZone(Application.ZONE_ID));
    }
}
