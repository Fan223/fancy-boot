package fancy.boot.core.annotation;

import lombok.experimental.UtilityClass;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * {@link Annotation} 解析器.
 *
 * @author Fan
 */
@UtilityClass
public class AnnotationResolver {

    /**
     * 解析方法或类上的注解.
     *
     * @param joinPoint      {@link ProceedingJoinPoint}
     * @param annotationType {@link Class}
     * @return {@link A}
     */
    public static <A extends Annotation> A resolve(ProceedingJoinPoint joinPoint, Class<A> annotationType) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        // 获取真正执行的方法(解决接口代理、CGLIB、桥接方法等问题)
        Method method = ClassUtils.getMostSpecificMethod(signature.getMethod(), targetClass);

        // 优先解析方法上的注解
        A annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (annotation != null) {
            return annotation;
        }
        // 方法没有则解析类上的注解
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, annotationType);
    }
}
