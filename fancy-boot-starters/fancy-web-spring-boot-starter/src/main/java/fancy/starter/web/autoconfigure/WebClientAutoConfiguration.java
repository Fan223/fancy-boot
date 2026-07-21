package fancy.starter.web.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@link WebClient} 自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnClass(WebClient.class)
public class WebClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder webClientBuilder() {
        // 配置策略, 取消内存缓冲区大小限制, 但注意超大数据可能造成内存溢出, 超大数据建议使用流式处理
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs().maxInMemorySize(-1)).build());
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
