package ez.spring.vertx.httpServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.http.HttpServerOptions;

@Lazy
@Import({VertxConfiguration.class, ServerProperties.class})
@ConditionalOnMissingBean(HttpServerOptions.class)
@Configuration
@ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
public class HttpServerConfiguration extends HttpServerOptions {
    public HttpServerConfiguration(ServerProperties serverProperties) {
        super();
        Integer port = serverProperties.getPort();
        setPort(port == null ? 8080 : port);
        setCompressionSupported(serverProperties.getCompression().getEnabled());
    }
}