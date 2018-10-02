package ez.spring.vertx;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        VertxProperties.class
})
public class VertxConfiguration {
    @ConditionalOnMissingBean(Vertx.class)
    @Bean
    public Vertx vertx(VertxOptions vertxOptions) {
        return Vertx.vertx(vertxOptions);
    }

    @ConditionalOnMissingBean(VertxOptions.class)
    @Bean
    public VertxOptions vertxOptions(
            VertxProperties vertxProperties,
            @Autowired(required = false) ClusterManager clusterManager
    ) {
        if (clusterManager != null) vertxProperties.setClusterManager(clusterManager);
        return vertxProperties;
    }

    @ConditionalOnMissingBean(ClusterManager.class)
    @Bean
    public ClusterManager clusterManager() {
        return null;
    }
}
