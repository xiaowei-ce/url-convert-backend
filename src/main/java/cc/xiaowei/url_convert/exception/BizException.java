package cc.xiaowei.url_convert.exception;

public class BizException extends BaseException {

    private BizException(Integer code,String message) {
        super(code,message);
    }

    public static void throw_(String msg) {
        throw new BizException(500, msg);
    }
}
