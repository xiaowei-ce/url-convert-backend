package cc.xiaowei.url_convert.handler;

import cc.xiaowei.url_convert.common.FrontPages;
import cc.xiaowei.url_convert.entity.Result;
import cc.xiaowei.url_convert.exception.BizException;
import cc.xiaowei.url_convert.exception.RedirectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(BizException.class)
    public Result<Object> bizException(BizException exp){
        log.error("{} {}",exp.getCode(),exp.getMessage(),exp);
        return Result.fail(exp.getCode(),  exp.getMessage());
    }

    @ExceptionHandler(RedirectException.class)
    public RedirectView redirectException(RedirectException exp){
        log.error("redirect err ",exp);
        RedirectView errView = new RedirectView(FrontPages.ERR_URL);
        Map<String, String> attrs = Map.of("cause", exp.getMessage());
        errView.setAttributesMap(attrs);
        return errView;
    }

    @ExceptionHandler(Exception.class)
    public RedirectView exception(Exception exp){
        log.error("unknow err ",exp);
        RedirectView errView = new RedirectView(FrontPages.ERR_URL);
        Map<String, String> attrs = Map.of("cause", exp.getMessage());
        errView.setAttributesMap(attrs);
        return errView;
    }

}
