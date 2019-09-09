package ez.spring.vertx.httpServer;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Lazy
@Configuration
@Import({VertxConfiguration.class, ServerProperties.class})
@ConditionalOnMissingBean(HttpServerOptions.class)
@ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
public class HttpServerConfiguration extends HttpServerOptions {
    public static final int DEFAULT_PORT = 8999;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean enabled = true;

    public HttpServerConfiguration(ServerProperties serverProperties) {
        super();
        Integer port = serverProperties.getPort();
        setPort(port == null || port < 0 ? DEFAULT_PORT : port);
        setCompressionSupported(serverProperties.getCompression().getEnabled());
    }

    @Lazy
    @Bean
    @ConditionalOnMissingBean(HttpServer.class)
    @Scope(scopeName = "thread", proxyMode = ScopedProxyMode.INTERFACES)
    public HttpServer httpServer(Vertx vertx, HttpServerConfiguration options) {
        if (options.isEnabled()) return vertx.createHttpServer(options);
        else return null;
    }
}