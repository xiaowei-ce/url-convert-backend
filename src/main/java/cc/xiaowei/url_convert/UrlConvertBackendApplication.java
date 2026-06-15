package cc.xiaowei.url_convert;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("cc.xiaowei.url_convert.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class UrlConvertBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlConvertBackendApplication.class, args);
    }

}
