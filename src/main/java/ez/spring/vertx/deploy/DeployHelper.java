package ez.spring.vertx.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

import ez.spring.vertx.EzJob;
import ez.spring.vertx.bean.Beans;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * this helper can wrap a deployment to make it running on vertx context instead of main thread
 */
public class DeployHelper extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(DeployHelper.class);
  private final DeployProps props;
  /**
   * promise for the real deployment id
   */
  private final Promise<String> deployPromise = Promise.promise();

  private DeployHelper(DeployProps props) {
    this.props = props;
  }

  private String getName() {
    String descriptor = props.getDescriptor();
    String qualifier = props.getBeanQualifier();
    return qualifier == null ? descriptor : descriptor + "(qualifier: " + qualifier + ")";
  }

  @Override
  public void start(Promise<Void> startPromise) {
    String name = getName();
    EzJob.create(vertx, "deploy verticle " + name, props.getTimeout())
      .thenSupply(() -> doDeploy(vertx, props))
      .then(deployPromise)
      .thenConsume(deploymentId -> log.info("deploy verticle success: [{}], id={}", name, deploymentId))
      .start(startPromise);
  }

  private static Future<String> doDeploy(Vertx vertx, DeployProps props) {
    String  descriptor = props.getDescriptor();
    if (descriptor.contains(":")) return vertx.deployVerticle(descriptor, props);
    else {
      Supplier<Verticle> provider = Beans.<Verticle>withDescriptor(
        descriptor
      ).withQualifier(
        props.getBeanQualifier()
      ).getFirstProvider();
      return vertx.deployVerticle(provider, props);
    }
  }

  public static Future<String> deploy(Vertx vertx, DeployProps props) {
    int instances = props.getInstances();
    if (instances > 1) {
      DeploymentOptions options = new DeploymentOptions();
      options.setInstances(instances);
      props.setInstances(1);
      DeployHelper helper = new DeployHelper(props);
      vertx.deployVerticle(helper, options);
      return helper.deployPromise.future();
    } else return doDeploy(vertx, props);
  }
}
