package fancy.validation.starter.autoconfigure;

import fancy.validation.starter.advice.ValidationExceptionAdvice;
import fancy.validation.starter.service.ValidatorService;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 参数校验自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
public class ValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidationExceptionAdvice fancyValidationExceptionAdvice() {
        return new ValidationExceptionAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidatorService fancyValidatorService(Validator validator) {
        return new ValidatorService(validator);
    }
}
