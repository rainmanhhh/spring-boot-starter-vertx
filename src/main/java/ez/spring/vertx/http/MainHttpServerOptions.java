package ez.spring.vertx.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.http.HttpServerOptions;

@Component
@ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
public class MainHttpServerOptions extends HttpServerOptions {
    private int port = 8999;

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public MainHttpServerOptions setPort(int port) {
        this.port = port;
        super.setPort(port);
        return this;
    }
}
