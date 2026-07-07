package fancy.starter.web.autoconfigure;

import fancy.starter.web.advice.GlobalExceptionAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * Web 自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
public class WebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionAdvice fancyGlobalExceptionAdvice() {
        return new GlobalExceptionAdvice();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                // 配置策略, 取消内存缓冲区大小限制, 但注意超大数据可能造成内存溢出, 超大数据建议使用流式处理
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs().maxInMemorySize(-1)).build());
    }

    @Bean
    public WebClient webClient() {
        return webClientBuilder().build();
    }
}
