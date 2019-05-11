package ez.spring.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import lombok.Data;

@Data
public class DeploymentOptionsEx extends DeploymentOptions {
    public static final Logger logger = LoggerFactory.getLogger(DeploymentOptionsEx.class);
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

    private CompletableFuture<String> doDeploy(Vertx vertx, Object verticle, boolean asyncTimeout) {
        logger.info("deploy verticle start: [{}]", Objects.requireNonNull(verticle));
        final FutureEx<String> future = FutureEx.future();
        if (verticle instanceof String) {
            vertx.deployVerticle((String) verticle, this, future);
        } else if (verticle instanceof Verticle) {
            vertx.deployVerticle((Verticle) verticle, this, future);
        } else {
            String message = "verticle class should be String or Verticle, but actually: " + verticle.getClass().getCanonicalName();
            if (future.fail(message)) return future;
            else return FutureEx.failedFuture(message);
        }
        long timeout = asyncTimeout ? getTimeout() : 0;
        return FutureEx.setTimeout(
                future, vertx, timeout, "deploy"
        ).thenApply(deploymentId -> {
            logger.info("deploy verticle success: [{}], id={}", verticle, deploymentId);
            return deploymentId;
        });
    }

    private CompletableFuture<String> doDeployAsync(Vertx vertx, Object verticle) {
        return doDeploy(vertx, verticle, true);
    }

    private String doDeploySync(Vertx vertx, Object verticle) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = doDeploy(vertx, verticle, false);
        long timeout = getTimeout();
        return timeout > 0 ? future.get(timeout, TimeUnit.SECONDS) : future.get();
    }

    public CompletableFuture<String> deploy(Vertx vertx, Verticle verticle) {
        return doDeployAsync(vertx, verticle);
    }

    public CompletableFuture<String> deploy(Vertx vertx, String descriptor) {
        return doDeployAsync(vertx, descriptor);
    }

    public String deploySync(Vertx vertx, Verticle verticle) throws InterruptedException, ExecutionException, TimeoutException {
        return doDeploySync(vertx, verticle);
    }

    public String deploySync(Vertx vertx, String descriptor) throws InterruptedException, ExecutionException, TimeoutException {
        return doDeploySync(vertx, descriptor);
    }
}