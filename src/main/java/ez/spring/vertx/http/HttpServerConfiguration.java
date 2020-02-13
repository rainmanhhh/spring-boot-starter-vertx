package ez.spring.vertx.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.http.HttpServerOptions;

@Configuration
@Import({VertxConfiguration.class, ServerProperties.class})
public class HttpServerConfiguration {
  @Lazy
  @ConditionalOnMissingBean(HttpServerOptions.class)
  @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-server")
  @Bean
  public HttpServerOptions httpServerOptions(ServerProperties serverProperties) {
    MainHttpServerOptions options = new MainHttpServerOptions();
    Integer port = serverProperties.getPort();
    options.setPort(port == null || port < 0 ? MainHttpServerOptions.DEFAULT_PORT : port);
    return options;
  }
}