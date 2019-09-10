package ez.spring.vertx.net;

import ez.spring.vertx.Main;
import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@Import(VertxConfiguration.class)
public class NetClientConfiguration {
    @Lazy
    @ConditionalOnMissingBean(value = NetClientOptions.class, annotation = Main.class)
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".net-client")
    @Bean
    public NetClientOptions netClientOptions() {
        return new NetClientOptions();
    }

    @Bean
    @ConditionalOnMissingBean(value = NetClient.class, annotation = Main.class)
    @Scope(scopeName = "thread", proxyMode = ScopedProxyMode.INTERFACES)
    public NetClient netClient(Vertx vertx, NetClientOptions options) {
        return vertx.createNetClient(options);
    }
}
