package ez.spring.vertx.http;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@Import({VertxConfiguration.class, ServerProperties.class})
public class HttpServerConfiguration {
    public static final int DEFAULT_PORT = 8999;

    @Lazy
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
    @ConditionalOnMissingBean(HttpServerOptions.class)
    @Bean
    public HttpServerOptions httpServerOptions() {
        return new HttpServerOptions().setPort(DEFAULT_PORT);
    }

    @Bean
    @ConditionalOnMissingBean(HttpServer.class)
    @Scope(scopeName = "thread", proxyMode = ScopedProxyMode.INTERFACES)
    public HttpServer httpServer(Vertx vertx, HttpServerOptions options) {
        return vertx.createHttpServer(options);
    }
}