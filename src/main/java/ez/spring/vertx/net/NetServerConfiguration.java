package ez.spring.vertx.net;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.net.NetServerOptions;

@Configuration
@Import(VertxConfiguration.class)
public class NetServerConfiguration {
  @Lazy
  @ConfigurationProperties(VertxConfiguration.PREFIX + ".net-server")
  @Bean
  public NetServerOptions netServerOptions() {
    return new NetServerOptions();
  }
}
