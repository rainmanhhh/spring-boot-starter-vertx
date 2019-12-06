package ez.spring.vertx.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.http.HttpServerOptions;

@Configuration
@Import({VertxConfiguration.class, ServerProperties.class})
public class HttpServerConfiguration {
    @Lazy
    @ConditionalOnMissingBean(HttpServerOptions.class)
    @Bean
    public HttpServerOptions httpServerOptions() {
        return new MainHttpServerOptions();
    }
}