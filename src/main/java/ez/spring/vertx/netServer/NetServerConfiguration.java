package ez.spring.vertx.netServer;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Lazy
@Configuration
@Import(VertxConfiguration.class)
@ConditionalOnMissingBean(NetServerOptions.class)
@ConfigurationProperties(VertxConfiguration.PREFIX + ".net-server")
public class NetServerConfiguration extends NetServerOptions {
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Lazy
    @Bean
    @ConditionalOnMissingBean(NetServer.class)
    @Scope(scopeName = "thread", proxyMode = ScopedProxyMode.INTERFACES)
    public NetServer netServer(Vertx vertx, NetServerConfiguration options) {
        if (options.isEnabled()) return vertx.createNetServer(options);
        else return null;
    }
}
