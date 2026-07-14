package fancy.starter.web.advice;

import fancy.boot.core.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理.
 *
 * @author Fan
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(Exception.class)
    public Response<String> handleOtherException(Exception exception) {
        log.error("系统异常: {}", exception.getMessage(), exception);
        return Response.fail("系统异常: " + exception.getMessage());
    }
}
