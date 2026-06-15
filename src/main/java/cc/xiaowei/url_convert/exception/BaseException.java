package cc.xiaowei.url_convert.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class BaseException extends RuntimeException{
    Integer code;

    public BaseException(Integer code,String message){
        super(message);
        this.code = code;
    }

}
