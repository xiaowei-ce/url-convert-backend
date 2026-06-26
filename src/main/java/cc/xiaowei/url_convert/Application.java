package cc.xiaowei.url_convert;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;

@SpringBootApplication
@MapperScan("cc.xiaowei.url_convert.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class Application {

    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
