package ez.spring.vertx.http;

import ez.spring.vertx.Main;
import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.http.HttpServerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

@Configuration
@Import({VertxConfiguration.class, ServerProperties.class})
public class HttpServerConfiguration {
    public static final int DEFAULT_PORT = 8999;

    /**
     * default port is {@link #DEFAULT_PORT}
     * @return default http server options
     */
    @Lazy
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
    @ConditionalOnMissingBean(value = HttpServerOptions.class, annotation = Main.class)
    @Main
    @Bean
    public HttpServerOptions httpServerOptions() {
        return new HttpServerOptions().setPort(DEFAULT_PORT);
    }
}