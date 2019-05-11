package ez.spring.vertx;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * auto deploy mainVerticle(bean with qualifier annotation {@link MainVerticle})
 * and verticles defined in application config file(prefix=vertx.verticles)
 */
public class AutoDeployer implements CommandLineRunner {
    private final ApplicationContext applicationContext;
    private final Vertx vertx;
    private final VertxProps vertxProps;
    private final Verticle mainVerticle;
    private final DeploymentOptionsEx mainVerticleDeploy;
    private final Logger logger = LoggerFactory.getLogger(getClass());

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
            logger.info("vertx.main-verticle.enabled is false. skip deploying MainVerticle");
            return FutureEx.succeededFuture();
        } else {
            if (mainVerticle == null) {
                logger.info("MainVerticle bean is null. skip deploying MainVerticle");
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
            ClassLoader classLoader = Objects.requireNonNull(applicationContext.getClassLoader());
            for (VerticleDeploy verticleDeploy : allDeploys) {
                if (!verticleDeploy.isEnabled()) {
                    logger.debug("skip disabled verticleDeploy, descriptor: {}, qualifier: {}",
                            verticleDeploy.getDescriptor(), verticleDeploy.getBeanQualifier());
                    continue;
                }
                String descriptor = verticleDeploy.getDescriptor();
                if (descriptor.contains(":")) {
                    future = future.thenComposeAsync(o -> verticleDeploy.deploy(vertx, descriptor));
                } else {
                    Verticle verticle;
                    if (applicationContext.containsBean(descriptor)) { // use key as bean name
                        verticle = applicationContext.getBean(descriptor, Verticle.class);
                    } else { // use key as class
                        @SuppressWarnings("unchecked")
                        Class<? extends Verticle> verticleType = (Class<? extends Verticle>) classLoader.loadClass(descriptor);
                        try {
                            String beanQualifier = verticleDeploy.getBeanQualifier();
                            if (beanQualifier == null)
                                verticle = applicationContext.getBean(verticleType);
                            else
                                verticle = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                                        applicationContext.getAutowireCapableBeanFactory(),
                                        verticleType,
                                        beanQualifier
                                );
                        } catch (NoSuchBeanDefinitionException beanNotFound) {
                            try {
                                verticle = verticleType.getConstructor().newInstance();
                            } catch (Exception e) {
                                e.addSuppressed(beanNotFound);
                                throw e;
                            }
                        }
                    }
                    final Verticle finalVerticle = verticle;
                    future = future.thenCompose(o -> verticleDeploy.deploy(vertx, finalVerticle));
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