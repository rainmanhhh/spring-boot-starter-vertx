package ez.spring.vertx.httpServer;

import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServerOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("vertx.http-server")
public class HttpServerProperties extends HttpServerOptions {
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @NestedConfigurationProperty
    private Http2Settings http2 = getInitialSettings();

    public void setHttp2(Http2Settings http2) {
        setInitialSettings(http2);
    }

    public Http2Settings getHttp2() {
        return http2;
    }
}
