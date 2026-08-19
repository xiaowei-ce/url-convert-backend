package cc.xiaowei.url_convert.controller;

import cc.xiaowei.url_convert.common.IdUriConvert;
import io.seruco.encoding.base62.Base62;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
public class MapURLControllerTest {


    @Test
    public void mapURLTest(){
    }


    @Test
    public void base62Test(){

        Base62 base62 = Base62.createInstance();

//         162671886836003L

        //id -> 10to62 -> base62
//
//        System.out.println(CachedIDFactory.extractYearMonth(303954835537920L));
//        System.out.println(CachedIDFactory.extractIncrement(303954835537920L));
//        System.out.println(IdUriConvert.id2Uri(303954835537920L));
        System.out.println(IdUriConvert.id2Uri(867793847189506L));


//        System.out.println(ConvertUtils.decimalToSixtyTwo(162671886836003L));
//        System.out.println(IdUriConvert.uri2IdElseNull("9CwJ26NFLB0"));


    }



}
