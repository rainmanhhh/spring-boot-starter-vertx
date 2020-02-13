package ez.spring.vertx.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.http.HttpClientOptions;

@Configuration
@Import(VertxConfiguration.class)
@ConditionalOnMissingBean(HttpClientOptions.class)
public class HttpClientConfiguration {
  @Lazy
  @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-client")
  @ConditionalOnMissingBean(HttpClientOptions.class)
  @Bean
  public HttpClientOptions httpClientOptions() {
    return new HttpClientOptions();
  }
}