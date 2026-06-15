package cc.xiaowei.url_convert.handler;

import cc.xiaowei.url_convert.entity.Result;
import cc.xiaowei.url_convert.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
@ResponseBody
public class ExceptionHandlers {

    @ExceptionHandler(BaseException.class)
    public Result<Object> baseException(BaseException exp){
        log.error("{} {}",exp.getCode(),exp.getMessage(),exp);
        return Result.fail(exp.getCode(),  exp.getMessage());
    }
}
