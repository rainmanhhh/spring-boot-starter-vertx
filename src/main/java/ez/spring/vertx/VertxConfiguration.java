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
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.cluster.ClusterManager;

@Configuration
public class VertxConfiguration {
    static {
        final String KEY = "vertx.logger-delegate-factory-class-name";
        if (System.getProperty(KEY) == null) {
            System.setProperty(
                    KEY,
                    SLF4JLogDelegateFactory.class.getCanonicalName()
            );
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public Vertx vertx(
            VertxProps vertxProps
    ) throws ExecutionException, InterruptedException, TimeoutException {
        final Vertx vertx;
        if (vertxProps.getEventBusOptions().isClustered()) {
            CompletableFuture<Vertx> future = new CompletableFuture<>();
            logger.info("waiting vertx join to cluster...");
            Vertx.clusteredVertx(vertxProps, asyncResult -> {
                if (asyncResult.succeeded()) future.complete(asyncResult.result());
                else future.completeExceptionally(asyncResult.cause());
            });
            vertx = future.get(vertxProps.getClusterJoinTimeout(), TimeUnit.SECONDS);
            logger.info("vertx join to cluster success");
        } else vertx = Vertx.vertx(vertxProps);
        return vertx;
    }

    @ConfigurationProperties("vertx")
    @ConditionalOnMissingBean(VertxProps.class)
    @Bean
    public VertxProps vertxProps(
            @Autowired(required = false) ClusterManager clusterManager,
            @Autowired(required = false) VertxMetricsFactory metricsFactory
    ) {
        VertxProps vertxProps = new VertxProps();
        if (clusterManager != null) vertxProps.setClusterManager(clusterManager);
        if (metricsFactory != null) vertxProps.getMetricsOptions().setFactory(metricsFactory);
        return vertxProps;
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

    @ConditionalOnMissingBean(value = Verticle.class, annotation = MainVerticle.class)
    @MainVerticle
    @Bean
    public Verticle mainVerticle() {
        return null;
    }

    @Bean
    public VertxCommandLineRunner vertxCommandLineRunner(
            Vertx vertx,
            VertxProps vertxProps,
            DeploymentOptions deploymentOptions,
            @Autowired(required = false) Verticle mainVerticle
    ) {
        return new VertxCommandLineRunner(
                vertx, vertxProps, mainVerticle, deploymentOptions
        );
    }
}