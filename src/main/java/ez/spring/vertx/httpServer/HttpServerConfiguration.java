package ez.spring.vertx.httpServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

@Import(VertxConfiguration.class)
@Configuration
public class HttpServerConfiguration {
    @Lazy
    @ConditionalOnMissingBean(HttpServer.class)
    @Bean
    public HttpServer httpServer(
            Vertx vertx,
            HttpServerOptions options
    ) {
        return vertx.createHttpServer(options);
    }

    @Lazy
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
    @ConditionalOnMissingBean(HttpServerOptions.class)
    @Bean
    public HttpServerOptions httpServerOptions() {
        return new HttpServerOptions().setPort(8999);
    }
}
