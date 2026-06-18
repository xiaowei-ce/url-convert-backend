package cc.xiaowei.url_convert.exception;

public class RedirectException extends RuntimeException {
    public RedirectException(String message) {
        super(message);
    }

    public static void throw_(String msg){
        throw new RedirectException(msg);
    }
}
