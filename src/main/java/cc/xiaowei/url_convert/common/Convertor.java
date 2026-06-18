package cc.xiaowei.url_convert.common;

import io.seruco.encoding.base62.Base62;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Convertor {

    private final static Base62 base62 = Base62.createInstance();


    public static <T> String convert(T id) {
        return convert(id.toString());
    }

    public static String convert(String id) {
        return convert(id, StandardCharsets.UTF_8);
    }

    public static String revert(String uri) {
       return revert(uri, StandardCharsets.UTF_8);
    }

    public static String convert(String id, Charset charset) {
        byte[] encoded = base62.encode(id.getBytes(charset));
        return new String(encoded, charset);
    }

    public static String revert(String uri, Charset charset) {
        byte[] bytes = uri.getBytes(charset);
        if (!base62.isBase62Encoding(bytes)){
            return null;
        }
        return new String(base62.decode(bytes), charset);
    }

}
