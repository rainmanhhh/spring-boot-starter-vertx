package ez.spring.vertx.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Objects;

import ez.spring.vertx.EzJob;
import ez.spring.vertx.util.EzUtil;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * deployment options with verticle descriptor and beanQualifier(valid only if descriptor is a bean class name) and order
 */
public class DeployProps extends DeploymentOptions {
  private static final Logger log = LoggerFactory.getLogger(DeployProps.class);
  private boolean enabled = false;
  private int order = 0;
  /**
   * timeout of deploying verticle.
   * unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
   * equals or less than 0 means wait forever
   */
  private long timeout = 30_000L;
  /**
   * verticle descriptor. ex: `xx.yy.ZVerticle`, `groovy:com.xx.yy.ZVerticle`, `service:http://xx.yy/zVerticle.zip`, `someBeanName`
   */
  private String descriptor;
  /**
   * bean qualifier. valid only if {@link #descriptor} is a bean class name
   */
  @Nullable
  private String beanQualifier;

  public DeployProps() {
    super();
  }

  public DeployProps(DeployProps other) {
    super(other);
    enabled = other.enabled;
    order = other.order;
    timeout = other.timeout;
    descriptor = other.getDescriptor();
    beanQualifier = other.getBeanQualifier();
  }

  public Future<String> deploy(Vertx vertx, Verticle verticle) {
    return createJob(vertx, verticle).start().future();
  }

  public Future<String> deploy(Vertx vertx, String descriptor) {
    return createJob(vertx, descriptor).start().future();
  }

  public String deploySync(Vertx vertx, Verticle verticle) {
    return createJob(vertx, verticle).join();
  }

  public String deploySync(Vertx vertx, String descriptor) {
    return createJob(vertx, descriptor).join();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public DeployProps setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public int getOrder() {
    return order;
  }

  public DeployProps setOrder(int order) {
    this.order = order;
    return this;
  }

  public long getTimeout() {
    return timeout;
  }

  public DeployProps setTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  public String getDescriptor() {
    return descriptor;
  }

  public DeployProps setDescriptor(String descriptor) {
    this.descriptor = Objects.requireNonNull(descriptor);
    return this;
  }

  @Nullable
  public String getBeanQualifier() {
    return beanQualifier;
  }

  public DeployProps setBeanQualifier(@Nullable String beanQualifier) {
    this.beanQualifier = beanQualifier;
    return this;
  }

  private EzJob<String> createJob(Vertx vertx, String verticle) {
    return EzJob.create(vertx, "deploy verticle " + verticle)
      .then((Object o, Promise<String> p) -> vertx.deployVerticle(verticle, this, p))
      .thenMap(deploymentId -> {
        log.info("deploy verticle success: [{}], id={}", verticle, deploymentId);
        return deploymentId;
      }).setTimeout(getTimeout());
  }

  private EzJob<String> createJob(Vertx vertx, Verticle verticle) {
    String verticleStr = EzUtil.toString(Objects.requireNonNull(verticle));
    return EzJob.create(vertx, "deploy verticle " + verticleStr)
      .then((Object o, Promise<String> p) -> vertx.deployVerticle(verticle, this, p))
      .thenMap(deploymentId -> {
        log.info("deploy verticle success: [{}], id={}", verticleStr, deploymentId);
        return deploymentId;
      }).setTimeout(getTimeout());
  }
}