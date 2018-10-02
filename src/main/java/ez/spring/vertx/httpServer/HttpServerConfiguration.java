package ez.spring.vertx.httpServer;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties(HttpServerProperties.class)
@Configuration
@Import(VertxConfiguration.class)
public class HttpServerConfiguration {
    @ConditionalOnMissingBean(HttpServer.class)
    @ConditionalOnBean(Vertx.class)
    @Bean
    public HttpServer httpServer(
            Vertx vertx,
            HttpServerProperties properties,
            @Autowired(required = false) Http2Settings http2Settings
    ) {
        if (http2Settings != null) properties.setInitialSettings(http2Settings);
        return properties.isEnabled() ? vertx.createHttpServer(properties) : null;
    }

    @ConditionalOnMissingBean(Http2Settings.class)
    @Bean
    public Http2Settings http2Settings() {
        return null;
    }
}
