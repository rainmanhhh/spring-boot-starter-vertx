package ez.spring.vertx.net;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.net.NetClientOptions;

@Configuration
@Import(VertxConfiguration.class)
public class NetClientConfiguration {
  @Lazy
  @ConfigurationProperties(VertxConfiguration.PREFIX + ".net-client")
  @Bean
  public NetClientOptions netClientOptions() {
    return new NetClientOptions();
  }
}
