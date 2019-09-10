package ez.spring.vertx.net;

import ez.spring.vertx.Main;
import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.net.NetClientOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

@Configuration
@Import(VertxConfiguration.class)
public class NetClientConfiguration {
    @Lazy
    @ConditionalOnMissingBean(value = NetClientOptions.class, annotation = Main.class)
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".net-client")
    @Main
    @Bean
    public NetClientOptions netClientOptions() {
        return new NetClientOptions();
    }
}
