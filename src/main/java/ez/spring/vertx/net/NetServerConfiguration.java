package ez.spring.vertx.net;

import ez.spring.vertx.Main;
import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@Import(VertxConfiguration.class)
public class NetServerConfiguration {
    @Lazy
    @ConditionalOnMissingBean(value = NetServerOptions.class, annotation = Main.class)
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".net-server")
    @Bean
    public NetServerOptions netServerOptions() {
        return new NetServerOptions();
    }

    @Bean
    @ConditionalOnMissingBean(value = NetServer.class, annotation = Main.class)
    @Scope(scopeName = "thread", proxyMode = ScopedProxyMode.INTERFACES)
    public NetServer netServer(Vertx vertx, NetServerOptions options) {
        return vertx.createNetServer(options);
    }
}
