package ez.spring.vertx.http;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;

@Configuration
@Import({VertxConfiguration.class, ServerProperties.class})
public class HttpServerConfiguration {
  @Lazy
  @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
  @Bean
  public MainHttpServerOptions httpServerOptions(ServerProperties serverProperties) {
    MainHttpServerOptions options = new MainHttpServerOptions();
    Integer port = serverProperties.getPort();
    if (port != null && port >= 0) options.setPort(port);
    return options;
  }
}