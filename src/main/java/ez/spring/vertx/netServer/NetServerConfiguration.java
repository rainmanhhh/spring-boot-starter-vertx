package ez.spring.vertx.netServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

@Import(VertxConfiguration.class)
@ConfigurationProperties("vertx.net-server")
@Configuration
public class NetServerConfiguration {
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @ConfigurationProperties("vertx.net-server.options")
    @ConditionalOnMissingBean(NetServerOptions.class)
    @Bean
    public NetServerOptions netServerOptions() {
        return new NetServerOptions();
    }

    @ConditionalOnMissingBean(HttpServer.class)
    @Bean
    public NetServer netServer(Vertx vertx, NetServerOptions options) {
        return isEnabled() ? vertx.createNetServer(options) : null;
    }
}
