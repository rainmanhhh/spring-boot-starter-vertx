package ez.spring.vertx.netServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

@Import(VertxConfiguration.class)
@ConfigurationProperties("vertx.net-server")
@Configuration
public class NetServerConfiguration {
    @Lazy
    @ConfigurationProperties("vertx.net-server.options")
    @ConditionalOnMissingBean(NetServerOptions.class)
    @Bean
    public NetServerOptions netServerOptions() {
        return new NetServerOptions();
    }

    @Lazy
    @ConditionalOnMissingBean(HttpServer.class)
    @Bean
    public NetServer netServer(Vertx vertx, NetServerOptions options) {
        return vertx.createNetServer(options);
    }
}
