package fancy.starter.validation.model;

/**
 * 字段校验错误详情.
 *
 * @author Fan
 */
public record FieldErrorDetail(String field, String message, Object rejectedValue) {
}
