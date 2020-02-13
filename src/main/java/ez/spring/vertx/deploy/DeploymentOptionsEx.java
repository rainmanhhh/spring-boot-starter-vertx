package ez.spring.vertx.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import ez.spring.vertx.EzJob;
import ez.spring.vertx.util.EzUtil;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

@SuppressWarnings("WeakerAccess")
public class DeploymentOptionsEx extends DeploymentOptions {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private boolean enabled = true;
  private int order = 0;
  /**
   * timeout of deploying verticle.
   * unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
   * equals or less than 0 means wait forever
   */
  private long timeout = 30_000L;

  public DeploymentOptionsEx() {
    super();
  }

  public DeploymentOptionsEx(DeploymentOptionsEx other) {
    super(other);
    enabled = other.enabled;
    order = other.order;
    timeout = other.timeout;
  }

  private EzJob<String> createJob(Vertx vertx, Object verticle) {
    String verticleStr = verticle instanceof String ? ((String) verticle) : EzUtil.toString(Objects.requireNonNull(verticle));
    return EzJob.create(vertx, "deploy verticle " + verticleStr)
      .then((Object o, Promise<String> p) -> {
        if (verticle instanceof String) {
          vertx.deployVerticle((String) verticle, this, p);
        } else if (verticle instanceof Verticle) {
          vertx.deployVerticle((Verticle) verticle, this, p);
        } else {
          p.fail("param `verticle` should be String or Verticle, but actually: " + verticle.getClass().getCanonicalName());
        }
      })
      .thenMap(deploymentId -> {
        log.info("deploy verticle success: [{}], id={}", verticleStr, deploymentId);
        return deploymentId;
      });
  }

  private Future<String> doDeployAsync(Vertx vertx, Object verticle) {
    return createJob(vertx, verticle).start(getTimeout()).future();
  }

  private String doDeploySync(Vertx vertx, Object verticle) {
    return createJob(vertx, verticle).join(getTimeout());
  }

  public Future<String> deploy(Vertx vertx, Verticle verticle) {
    return doDeployAsync(vertx, verticle);
  }

  public Future<String> deploy(Vertx vertx, String descriptor) {
    return doDeployAsync(vertx, descriptor);
  }

  public String deploySync(Vertx vertx, Verticle verticle) {
    return doDeploySync(vertx, verticle);
  }

  public String deploySync(Vertx vertx, String descriptor) {
    return doDeploySync(vertx, descriptor);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public DeploymentOptionsEx setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public int getOrder() {
    return order;
  }

  public DeploymentOptionsEx setOrder(int order) {
    this.order = order;
    return this;
  }

  public long getTimeout() {
    return timeout;
  }

  public DeploymentOptionsEx setTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }
}