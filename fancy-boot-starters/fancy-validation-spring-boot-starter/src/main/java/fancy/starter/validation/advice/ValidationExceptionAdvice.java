package fancy.starter.validation.advice;

import fancy.boot.core.http.Response;
import fancy.starter.validation.model.FieldErrorDetail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Set;

/**
 * 参数校验异常处理.
 *
 * @author Fan
 */
@Slf4j
@RestControllerAdvice
public class ValidationExceptionAdvice {

    /**
     * {@link ConstraintViolationException} 处理, 简单参数校验.
     *
     * @param exception {@link ConstraintViolationException}
     * @return {@link Response}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<List<FieldErrorDetail>> handleConstraintViolationException(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        String message = "简单参数校验失败";
        log.warn("{}: {}", message, violations);
        if (log.isDebugEnabled()) {
            log.debug("{}堆栈", message, exception);
        }

        List<FieldErrorDetail> details = violations.stream().map(violation -> new FieldErrorDetail(
                        violation.getPropertyPath().toString(), violation.getMessage(), violation.getInvalidValue()))
                .toList();
        return Response.of(HttpStatus.BAD_REQUEST.value(), message, details);
    }

    /**
     * {@link MethodArgumentNotValidException} 处理, 实体类参数校验.
     *
     * @param exception {@link MethodArgumentNotValidException}
     * @return {@link Response}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<List<FieldErrorDetail>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        String message = "实体类参数校验失败";
        logFieldErrors(message, fieldErrors, exception);
        List<FieldErrorDetail> details = fieldErrors.stream()
                .map(fieldError -> new FieldErrorDetail(
                        fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()))
                .toList();
        return Response.of(HttpStatus.BAD_REQUEST.value(), message, details);
    }

    /**
     * {@link BindException} 处理, 表单参数绑定校验.
     *
     * @param exception {@link BindException}
     * @return {@link Response}
     */
    @ExceptionHandler(BindException.class)
    public Response<List<FieldErrorDetail>> handleBindException(BindException exception) {
        List<FieldError> fieldErrors = exception.getFieldErrors();

        String message = "表单参数绑定失败";
        logFieldErrors(message, fieldErrors, exception);
        List<FieldErrorDetail> details = fieldErrors.stream()
                .map(fieldError -> new FieldErrorDetail(
                        fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()))
                .toList();
        return Response.of(HttpStatus.BAD_REQUEST.value(), message, details);
    }

    /**
     * {@link HandlerMethodValidationException} 处理, 方法参数校验.
     *
     * @param exception {@link HandlerMethodValidationException}
     * @return {@link Response}
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Response<List<FieldErrorDetail>> handleMethodValidationException(HandlerMethodValidationException exception) {
        List<FieldErrorDetail> details = exception.getAllErrors().stream()
                .map(resolvable -> new FieldErrorDetail(null, resolvable.getDefaultMessage(), null))
                .toList();

        String message = "方法参数校验失败";
        log.warn("{}: {}", message, details);
        if (log.isDebugEnabled()) {
            log.debug("{}堆栈", message, exception);
        }
        return Response.of(HttpStatus.BAD_REQUEST.value(), message, details);
    }

    private void logFieldErrors(String message, List<FieldError> fieldErrors, Throwable exception) {
        log.warn("{}: {}", message, fieldErrors);
        if (log.isDebugEnabled()) {
            log.debug("{}堆栈", message, exception);
        }
    }
}
