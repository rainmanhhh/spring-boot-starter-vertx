package cn.ez.vertx;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VertxProperties.class)
public class VertxConfiguration {
    @ConditionalOnMissingBean(Vertx.class)
    @Bean
    public Vertx vertx(VertxOptions vertxOptions) {
        return Vertx.vertx(vertxOptions);
    }

    @ConditionalOnMissingBean(VertxOptions.class)
    @ConfigurationProperties("vertx")
    @Bean
    public VertxOptions vertxOptions(
            @Autowired(required = false) ClusterManager clusterManager,
            MetricsOptions metricsOptions,
            EventBusOptions eventBusOptions,
            AddressResolverOptions addressResolverOptions
    ) {
        return new VertxOptions()
                .setClusterManager(clusterManager)
                .setMetricsOptions(metricsOptions)
                .setEventBusOptions(eventBusOptions)
                .setAddressResolverOptions(addressResolverOptions);
    }

    static public class ClusterManagerFactory extends Factory<ClusterManager> {
    }

    @ConditionalOnMissingBean(ClusterManagerFactory.class)
    @ConfigurationProperties("vertx.cluster-manager")
    @Bean
    public ClusterManagerFactory clusterManagerFactory() {
        return new ClusterManagerFactory();
    }

    @ConditionalOnMissingBean(ClusterManager.class)
    @Bean
    public ClusterManager clusterManager(ClusterManagerFactory factory) throws Exception {
        return factory.createInstance();
    }

    @ConditionalOnMissingBean(MetricsOptions.class)
    @ConfigurationProperties("vertx.metrics-options")
    @Bean
    public MetricsOptions metricsOptions(
            @Autowired(required = false) VertxMetricsFactory factory
    ) {
        return new MetricsOptions().setFactory(factory);
    }

    static public class VertxMetricsFactoryFactory extends Factory<VertxMetricsFactory> {
    }

    @ConditionalOnMissingBean(VertxMetricsFactoryFactory.class)
    @ConfigurationProperties("vertx.metrics-options.factory")
    @Bean
    public VertxMetricsFactoryFactory vertxMetricsFactoryFactory() {
        return new VertxMetricsFactoryFactory();
    }

    @ConditionalOnMissingBean(VertxMetricsFactory.class)
    @Bean
    public VertxMetricsFactory vertxMetricsFactory(VertxMetricsFactoryFactory factory) throws Exception {
        return factory.createInstance();
    }

    @ConditionalOnMissingBean(EventBusOptions.class)
    @ConfigurationProperties("vertx.event-bus-options")
    @Bean
    public EventBusOptions eventBusOptions() {
        return new EventBusOptions();
    }

    @ConditionalOnMissingBean(AddressResolverOptions.class)
    @ConfigurationProperties("vertx.address-resolver-options")
    @Bean
    public AddressResolverOptions addressResolverOptions() {
        return new AddressResolverOptions();
    }

    @ConditionalOnMissingBean(HttpServerOptions.class)
    @ConditionalOnProperty(value = "vertx.http-server-enabled", havingValue = "true")
    @ConfigurationProperties("vertx.http-server-options")
    @Bean
    public HttpServerOptions httpServerOptions() {
        return new HttpServerOptions();
    }

    @ConditionalOnBean(HttpServerOptions.class)
    @ConditionalOnMissingBean(HttpServer.class)
    @Bean
    public HttpServer httpServer(Vertx vertx, HttpServerOptions options) {
        return vertx.createHttpServer(options);
    }
}
