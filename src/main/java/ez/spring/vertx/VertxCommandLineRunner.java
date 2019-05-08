package ez.spring.vertx;

import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VertxCommandLineRunner implements CommandLineRunner {
    private final Vertx vertx;
    private final VertxProps vertxProps;
    private final Verticle mainVerticle;
    private final DeploymentOptions deploymentOptions;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public VertxCommandLineRunner(
            Vertx vertx,
            VertxProps vertxProps,
            Verticle mainVerticle,
            DeploymentOptions deploymentOptions
    ) {
        this.vertx = vertx;
        this.vertxProps = vertxProps;
        this.mainVerticle = mainVerticle;
        this.deploymentOptions = deploymentOptions;
    }

    @Override
    public void run(String... args) throws Exception {
        if (mainVerticle != null) {
            CompletableFuture<String> future = new CompletableFuture<>();
            logger.info("waiting vertx deploy MainVerticle...");
            vertx.deployVerticle(mainVerticle, deploymentOptions, event -> {
                if (event.succeeded()) future.complete(event.result());
                else future.completeExceptionally(event.cause());
            });
            String deploymentId = future.get(vertxProps.getMainVerticleDeployTimeout(), TimeUnit.SECONDS);
            logger.info("vertx deploy MainVerticle success, deploymentId={}", deploymentId);
        }
    }
}
