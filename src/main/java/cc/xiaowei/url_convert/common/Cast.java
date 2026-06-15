package cc.xiaowei.url_convert.common;

public class Cast {
    public static <T> T cast(Object object, Class<T> t_clazz) {
            if (object == null || t_clazz == null){
                return null;
            }
        return t_clazz.cast(object);
    }
}
