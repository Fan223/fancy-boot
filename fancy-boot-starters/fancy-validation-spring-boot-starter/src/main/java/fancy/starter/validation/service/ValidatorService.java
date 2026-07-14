package fancy.starter.validation.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * 参数校验服务.
 *
 * @author Fan
 */
@AllArgsConstructor
public class ValidatorService {

    private final Validator validator;

    /**
     * 对实体对象执行校验, 失败时抛出 {@link ConstraintViolationException}.
     *
     * @param obj    校验对象
     * @param groups 校验组
     * @param <T>    对象类型
     */
    public <T> void validate(T obj, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(obj, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("参数校验失败: " + violations.iterator().next().getMessage(), violations);
        }
    }
}
