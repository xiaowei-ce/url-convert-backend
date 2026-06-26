package cc.xiaowei.url_convert.common;

import io.seruco.encoding.base62.Base62;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class IdBase62Convertor {

    private final static Base62 base62 = Base62.createInstance();


    public static <T> String idTobase62str(T id) {
        return idTobase62str(id.toString());
    }

    public static String idTobase62str(String id) {
        return idTobase62str(id, StandardCharsets.UTF_8);
    }

    public static String base62ToIdStr(String uri) {
       return base62ToIdStr(uri, StandardCharsets.UTF_8);
    }

    public static String idTobase62str(String id, Charset charset) {
        byte[] encoded = base62.encode(id.getBytes(charset));
        return new String(encoded, charset);
    }

    public static String base62ToIdStr(String uri, Charset charset) {
        byte[] bytes = uri.getBytes(charset);
        if (!base62.isBase62Encoding(bytes)){
            return null;
        }
        return new String(base62.decode(bytes), charset);
    }

}
