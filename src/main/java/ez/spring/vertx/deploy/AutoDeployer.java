package ez.spring.vertx.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import ez.spring.vertx.Beans;
import ez.spring.vertx.FutureEx;
import ez.spring.vertx.MainVerticle;
import ez.spring.vertx.VertxProps;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * auto deploy mainVerticle(bean with qualifier annotation {@link MainVerticle})
 * and verticles defined in application config file(prefix=vertx.verticles)
 */
public class AutoDeployer implements CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ApplicationContext applicationContext;
    private final Vertx vertx;
    private final VertxProps vertxProps;
    private final Verticle mainVerticle;
    private final DeploymentOptionsEx mainVerticleDeploy;

    public AutoDeployer(
            ApplicationContext applicationContext,
            Vertx vertx,
            VertxProps vertxProps,
            @Nullable Verticle mainVerticle,
            DeploymentOptionsEx mainVerticleDeploy
    ) {
        this.applicationContext = applicationContext;
        this.vertx = vertx;
        this.vertxProps = vertxProps;
        this.mainVerticle = mainVerticle;
        this.mainVerticleDeploy = mainVerticleDeploy;
    }

    private CompletableFuture<String> deployMainVerticle() {
        if (!mainVerticleDeploy.isEnabled()) {
            log.info("vertx.main-verticle.enabled is false. skip deploying MainVerticle");
            return FutureEx.succeededFuture();
        } else {
            if (mainVerticle == null) {
                log.info("MainVerticle bean is null. skip deploying MainVerticle");
                mainVerticleDeploy.setEnabled(false);
                return FutureEx.succeededFuture();
            } else {
                return mainVerticleDeploy.deploy(vertx, mainVerticle);
            }
        }
    }

    private CompletableFuture<String> deployVerticles() {
        try {
            // merge VerticleDeploy beans & VerticleDeploy configList(sort by order)
            Collection<VerticleDeploy> beans = applicationContext.getBeansOfType(VerticleDeploy.class).values();
            List<VerticleDeploy> configList = vertxProps.getVerticles();
            List<VerticleDeploy> allDeploys = new ArrayList<>(beans.size() + configList.size());
            allDeploys.addAll(beans);
            allDeploys.addAll(configList);
            allDeploys.sort(Comparator.comparingInt(DeploymentOptionsEx::getOrder));
            // deploy verticles in the list one by one
            CompletableFuture<String> future = CompletableFuture.completedFuture(null);
            for (VerticleDeploy verticleDeploy : allDeploys) {
                if (!verticleDeploy.isEnabled()) {
                    log.debug("skip disabled verticleDeploy, descriptor: {}, qualifier: {}",
                            verticleDeploy.getDescriptor(), verticleDeploy.getBeanQualifier());
                    continue;
                }
                String descriptor = verticleDeploy.getDescriptor();
                if (descriptor.contains(":")) {
                    future = future.thenComposeAsync(o -> verticleDeploy.deploy(vertx, descriptor));
                } else {
                    Verticle verticle = Beans.get(descriptor, verticleDeploy.getBeanQualifier());
                    future = future.thenCompose(o -> verticleDeploy.deploy(vertx, verticle));
                }
            }
            return future;
        } catch (Exception e) {
            return FutureEx.failedFuture(e);
        }
    }

    @Override
    public void run(String... args) {
        CompletableFuture<?> future = FutureEx.succeededFuture()
                .thenCompose(o -> deployMainVerticle())
                .thenCompose(o -> deployVerticles())
                .thenCompose(lastDeploymentId -> {
                    if (lastDeploymentId == null && !mainVerticleDeploy.isEnabled()) {
                        return FutureEx.failedFuture("no enabled mainVerticle, no configured verticles");
                    } else return FutureEx.succeededFuture(lastDeploymentId);
                });
        FutureEx.setTimeout(future, vertx, vertxProps.getDeployTimeout(), "deploy").join();
    }
}