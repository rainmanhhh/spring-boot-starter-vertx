package ez.spring.vertx.netServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.net.NetServerOptions;

@Configuration
public class NetServerConfiguration extends NetServerOptions {
    @Lazy
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".net-server")
    @ConditionalOnMissingBean(NetServerOptions.class)
    @Bean
    public NetServerOptions netServerOptions() {
        return new NetServerOptions();
    }
}
