package ez.spring.vertx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.cluster.ClusterManager;

@Configuration
public class VertxConfiguration {
    @Bean
    public Vertx vertx(VertxOptions vertxOptions) throws ExecutionException, InterruptedException, TimeoutException {
        if (vertxOptions.getEventBusOptions().isClustered()) {
            CompletableFuture<Vertx> future = new CompletableFuture<>();
            Vertx.clusteredVertx(vertxOptions, asyncResult -> {
                if (asyncResult.succeeded()) future.complete(asyncResult.result());
                else future.completeExceptionally(asyncResult.cause());
            });
            return future.get(30, TimeUnit.SECONDS);
        } else return Vertx.vertx(vertxOptions);
    }

    @ConfigurationProperties("vertx")
    @ConditionalOnMissingBean(VertxOptions.class)
    @Bean
    public VertxOptions vertxOptions(
            @Autowired(required = false) ClusterManager clusterManager,
            @Autowired(required = false) VertxMetricsFactory metricsFactory
    ) {
        VertxOptions options = new VertxOptions();
        if (clusterManager != null) options.setClusterManager(clusterManager);
        if (metricsFactory != null) options.getMetricsOptions().setFactory(metricsFactory);
        return options;
    }

    @ConditionalOnMissingBean(ClusterManager.class)
    @Bean
    public ClusterManager clusterManager() {
        return null;
    }

    @ConditionalOnMissingBean(VertxMetricsFactory.class)
    @Bean
    public VertxMetricsFactory metricsFactory() {
        return null;
    }

    @ConfigurationProperties("vertx.deployment")
    @ConditionalOnMissingBean(DeploymentOptions.class)
    @Bean
    public DeploymentOptions deploymentOptions() {
        return new DeploymentOptions();
    }
}