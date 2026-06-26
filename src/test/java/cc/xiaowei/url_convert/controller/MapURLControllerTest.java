package cc.xiaowei.url_convert.controller;

import io.seruco.encoding.base62.Base62;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@Slf4j
public class MapURLControllerTest {

    @Autowired
    MapURLController mapURLController;

    @Test
    public void mapURLTest(){
    }


    @Test
    public void base62Test(){

        Base62 base62 = Base62.createInstance();

        final byte[] target = "MUMpmLOFEcqy0Y9BhJD9".getBytes(StandardCharsets.UTF_8);
        final byte[] origin = "162671886836003".getBytes(StandardCharsets.UTF_8);

         log.info("encoded -> {}", new String(base62.encode(origin),StandardCharsets.UTF_8));
         log.info("decoded -> {}", new String(base62.decode(target),StandardCharsets.UTF_8));

    }
}
