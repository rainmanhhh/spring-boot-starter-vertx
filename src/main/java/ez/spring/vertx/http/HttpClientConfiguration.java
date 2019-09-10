package ez.spring.vertx.http;

import ez.spring.vertx.Main;
import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@Import(VertxConfiguration.class)
@ConditionalOnMissingBean(HttpClientOptions.class)
public class HttpClientConfiguration {
    @Lazy
    @ConditionalOnMissingBean(value = HttpClientOptions.class, annotation = Main.class)
    @ConfigurationProperties(VertxConfiguration.PREFIX + ".http-client")
    @Bean
    public HttpClientOptions httpClientOptions() {
        return new HttpClientOptions();
    }

    @Bean
    @ConditionalOnMissingBean(value = HttpClient.class, annotation = Main.class)
    @Scope(scopeName = "thread", proxyMode = ScopedProxyMode.INTERFACES)
    public HttpClient httpClient(Vertx vertx, HttpClientOptions options) {
        return vertx.createHttpClient(options);
    }
}