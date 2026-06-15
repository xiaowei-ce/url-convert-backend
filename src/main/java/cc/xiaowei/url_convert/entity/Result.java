package cc.xiaowei.url_convert.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Result <T> {
    private final Integer code;
    private final String msg;
    private final T data;

    public static Result<Object> success(){
        return new Result<>(200,"success",null);
    }

    public static <T> Result<T> success(T  data){
        return new Result<>(200,"success",data);
    }

    public static Result<Object> fail(Integer code, String msg){
        return new Result<>(code, msg, null);
    }
}
