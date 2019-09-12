package ez.spring.vertx.deploy;

import ez.spring.vertx.Beans;
import ez.spring.vertx.EzJob;
import ez.spring.vertx.Main;
import ez.spring.vertx.VertxProps;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * auto deploy mainVerticle(bean of class {@link Verticle} with qualifier annotation {@link Main})
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

    private Future<String> deployMainVerticle() {
        if (!mainVerticleDeploy.isEnabled()) {
            log.info("vertx.main-verticle.enabled is false. skip deploying MainVerticle");
            return Future.succeededFuture();
        } else {
            if (mainVerticle == null) {
                log.info("MainVerticle bean is null. skip deploying MainVerticle");
                mainVerticleDeploy.setEnabled(false);
                return Future.succeededFuture();
            } else {
                return mainVerticleDeploy.deploy(vertx, mainVerticle);
            }
        }
    }

    private Future<String> deployVerticles() {
        try {
            // merge VerticleDeploy beans & VerticleDeploy configList(sort by order)
            Collection<VerticleDeploy> beans = applicationContext.getBeansOfType(VerticleDeploy.class).values();
            List<VerticleDeploy> configList = vertxProps.getVerticles();
            List<VerticleDeploy> allDeploys = new ArrayList<>(beans.size() + configList.size());
            allDeploys.addAll(beans);
            allDeploys.addAll(configList);
            allDeploys.sort(Comparator.comparingInt(DeploymentOptionsEx::getOrder));
            // deploy verticles in the list one by one
            Future<String> future = Future.succeededFuture();
            for (VerticleDeploy verticleDeploy : allDeploys) {
                if (!verticleDeploy.isEnabled()) {
                    log.debug("skip disabled verticleDeploy, descriptor: {}, qualifier: {}",
                            verticleDeploy.getDescriptor(), verticleDeploy.getBeanQualifier());
                    continue;
                }
                String descriptor = verticleDeploy.getDescriptor();
                if (descriptor.contains(":")) {
                    future = future.compose(o -> verticleDeploy.deploy(vertx, descriptor));
                } else {
                    Verticle verticle = (Verticle) Beans.withDescriptor(descriptor).withQualifier(verticleDeploy.getBeanQualifier()).get();
                    future = future.compose(o -> verticleDeploy.deploy(vertx, verticle));
                }
            }
            return future;
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public void run(String... args) {
        EzJob.create(vertx, "deploy verticles")
                .addStep(o -> deployMainVerticle())
                .addStep(o -> deployVerticles())
                .addStep(lastDeploymentId -> {
                    if (lastDeploymentId == null && !mainVerticleDeploy.isEnabled()) {
                        log.warn("no enabled mainVerticle, no configured verticles");
                    }
                    return Future.succeededFuture(lastDeploymentId);
                })
                .startSyncWait(vertxProps.getDeployTimeout());
    }
}