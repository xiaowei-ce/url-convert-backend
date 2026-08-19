package cc.xiaowei.url_convert.exception;

public class NotFoundException extends RuntimeException {

    NotFoundException(String msg){
        super(msg);
    }

    public static void _throw(){
        throw new NotFoundException(null);
    }

    public static void _throw(String msg){
        throw new NotFoundException(msg);
    }
}
