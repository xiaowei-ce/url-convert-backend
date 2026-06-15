package cc.xiaowei.url_convert.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisIdTest {

    @Autowired
    RedisId redisId;

    @Test
    public void getTest(){
//        for (int i = 0; i < 5000; i++) {
//            redisId.get();
//        }
        System.out.println(redisId.get());
    }
}
