package cc.xiaowei.url_convert.common;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisIDFactoryTest {


    @Autowired
    private CachedIDFactory cachedIDFactory;


    @Test
    public void nextTest(){

    }


    @Test
    public void timeTest(){

        StopWatch watch = new StopWatch();
        watch.start();
        for (int i = 0; i < 250; i++) {
            for (int j = 0; j < 1000; j++) {
                cachedIDFactory.next();
            }
        }
        watch.stop();

        System.out.println(watch.getTime(TimeUnit.MILLISECONDS));

    }
}
