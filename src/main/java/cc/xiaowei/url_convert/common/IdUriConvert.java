package cc.xiaowei.url_convert.common;

import io.seruco.encoding.base62.Base62;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class IdUriConvert {

    private final static Base62 base62 = Base62.createInstance();
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    //id -> 10t62 -> base62
    public static String id2Uri(Long id) {
        String st = ConvertUtils.decimalToSixtyTwo(id);
        byte[] encoded = base62.encode(st.getBytes(CHARSET));
        return new String(encoded, CHARSET);
    }

    //base62 -> 62t10 -> id
    public static Long uri2IdElseNull(String uri) {

        byte[] bytes = uri.getBytes(CHARSET);
        if (!base62.isBase62Encoding(bytes)){
            return null;
        }
        String decodeStr = new String(base62.decode(bytes), CHARSET);

        return ConvertUtils.sixtyTwoToDecimal(decodeStr);
    }

}
