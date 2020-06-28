package ez.spring.vertx.deploy;

import org.springframework.lang.Nullable;

import java.util.Objects;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * deployment options with verticle descriptor and beanQualifier(valid only if descriptor is a bean class name) and order
 */
public class DeployProps extends DeploymentOptions {
  private boolean enabled = true;
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

  public Future<String> deploy(Vertx vertx) {
    return DeployHelper.deploy(vertx, this);
  }
}