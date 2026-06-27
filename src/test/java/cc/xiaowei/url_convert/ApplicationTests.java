package cc.xiaowei.url_convert;

import cc.xiaowei.url_convert.mapper.URLMapMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;


@SpringBootTest
class ApplicationTests {

    @Autowired
    private URLMapMapper uRLMapMapper;

    @Test
    void contextLoads() throws SQLException {
        System.out.println(uRLMapMapper.selectById(867793847189506L));
    }

}
