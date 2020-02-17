package ez.spring.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ez.spring.vertx.deploy.AutoDeployer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.core.shareddata.SharedData;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.cluster.ClusterManager;

@Configuration
public class VertxConfiguration {
  public static final String PREFIX = "vertx";
  private static ApplicationContext applicationContext = null;
  private static ActiveProfiles activeProfiles = null;

  static {
    final String LOGGER_DELEGATE_KEY = "vertx.logger-delegate-factory-class-name";
    if (System.getProperty(LOGGER_DELEGATE_KEY) == null)
      System.setProperty(LOGGER_DELEGATE_KEY, SLF4JLogDelegateFactory.class.getCanonicalName());
  }

  private final Logger log = LoggerFactory.getLogger(getClass());

  public VertxConfiguration(ApplicationContext applicationContext) {
    VertxConfiguration.applicationContext = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    ApplicationContext value = applicationContext;
    if (value == null) throw new RuntimeException("applicationContext not set yet");
    return value;
  }

  public static ActiveProfiles getActiveProfiles() {
    ActiveProfiles value = activeProfiles;
    if (value == null) throw new RuntimeException("activeProfiles not set yet");
    return value;
  }

  @Bean
  public Vertx vertx(VertxProps vertxProps) throws ExecutionException, InterruptedException, TimeoutException {
    final Vertx vertx;
    if (vertxProps.getEventBusOptions().isClustered()) {
      CompletableFuture<Vertx> future = new CompletableFuture<>();
      log.info("waiting vertx join to cluster...");
      Vertx.clusteredVertx(vertxProps, EzPromise.fromCompletableFuture(future));
      long clusterJoinTimeout = vertxProps.getClusterJoinTimeout();
      vertx = clusterJoinTimeout > 0 ?
        future.get(clusterJoinTimeout, TimeUnit.MILLISECONDS) : future.get();
      log.info("vertx join to cluster success");
    } else vertx = Vertx.vertx(vertxProps);
    return vertx;
  }

  @Bean
  public ActiveProfiles activeProfiles(ApplicationContext ac) {
    ActiveProfiles value = new ActiveProfiles(Arrays.asList(ac.getEnvironment().getActiveProfiles()));
    VertxConfiguration.activeProfiles = value;
    return value;
  }

  @ConfigurationProperties(PREFIX)
  @ConditionalOnMissingBean
  @Bean
  public VertxProps vertxProps(
    ActiveProfiles activeProfiles,
    @Autowired(required = false) ClusterManager clusterManager,
    @Autowired(required = false) VertxMetricsFactory metricsFactory
  ) {
    VertxProps vertxProps = new VertxProps();
    if (clusterManager != null) vertxProps.setClusterManager(clusterManager);
    if (metricsFactory != null) vertxProps.getMetricsOptions().setFactory(metricsFactory);
    if (activeProfiles.isDev()) { // use large timeout for development
      vertxProps
        .setMaxEventLoopExecuteTime(2_000_000_000_000_000L) // 2 million seconds
        .setMaxWorkerExecuteTime(60_000_000_000_000_000L) // 60 million seconds
        .setBlockedThreadCheckInterval(1_000_000_000L); // 1 million seconds
    }
    return vertxProps;
  }

  @Nullable
  @ConditionalOnMissingBean
  @Bean
  public ClusterManager clusterManager() {
    return null;
  }

  @Nullable
  @ConditionalOnMissingBean
  @Bean
  public VertxMetricsFactory metricsFactory() {
    return null;
  }

  @Bean
  public AutoDeployer autoDeployer(Vertx vertx, VertxProps vertxProps) {
    return new AutoDeployer(applicationContext, vertx, vertxProps);
  }

  @Bean
  public EventBus eventBus(Vertx vertx) {
    return vertx.eventBus();
  }

  @Bean
  public FileSystem fileSystem(Vertx vertx) {
    return vertx.fileSystem();
  }

  @Bean
  public SharedData sharedData(Vertx vertx) {
    return vertx.sharedData();
  }
}