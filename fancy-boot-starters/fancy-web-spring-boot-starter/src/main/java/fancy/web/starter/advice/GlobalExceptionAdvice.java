package fancy.web.starter.advice;

import fancy.boot.core.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理.
 *
 * @author Fan
 */
@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public Response<String> handleOtherException(Exception exception) {
        LOGGER.error("系统异常: {}", exception.getMessage(), exception);
        return Response.fail("系统异常: " + exception.getMessage());
    }
}
