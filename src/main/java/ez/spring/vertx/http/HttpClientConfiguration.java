package ez.spring.vertx.http;

import ez.spring.vertx.Main;
import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.http.HttpClientOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

@Configuration
@Import(VertxConfiguration.class)
@ConditionalOnMissingBean(HttpClientOptions.class)
public class HttpClientConfiguration {
    @Lazy
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-client")
    @ConditionalOnMissingBean(value = HttpClientOptions.class, annotation = Main.class)
    @Main
    @Bean
    public HttpClientOptions httpClientOptions() {
        return new HttpClientOptions();
    }
}