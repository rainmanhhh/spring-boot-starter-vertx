package ez.spring.vertx.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import ez.spring.vertx.EzJob;
import ez.spring.vertx.VertxProps;
import ez.spring.vertx.bean.Beans;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * auto run when springboot application started. <br/>
 * 1. deploy verticles defined by {@link VerticleDeploy} beans. <br/>
 * 2. deploy verticles defined in application.yml(with deploy=true). <br/>
 * 3. deploy class annotated with {@link SpringBootApplication} if it's a verticle. <br/>
 * 4. deploy verticle beans annotated with {@link AutoDeploy}. <br/>
 * NOTICE: verticles in step 1 & 2 will be sorted by {@link VerticleDeploy#getOrder()}
 */
public class AutoDeployer implements SmartApplicationListener {
  private final Vertx vertx;
  private final VertxProps vertxProps;
  private Logger log = LoggerFactory.getLogger(getClass());

  public AutoDeployer(Vertx vertx, VertxProps vertxProps) {
    this.vertx = vertx;
    this.vertxProps = vertxProps;
  }

  private int doDeploy() {
    // merge VerticleDeploy beans & VerticleDeploy configList(sort by order)
    // 1.VerticleDeploy configList
    Collection<? extends VerticleDeploy> beans = Beans.withType(VerticleDeploy.class).getBeans();
    List<VerticleDeploy> configList = vertxProps.getVerticles();
    ArrayList<VerticleDeploy> allDeploys = new ArrayList<>();
    allDeploys.addAll(beans);
    allDeploys.addAll(configList);
    allDeploys.sort(Comparator.comparingInt((DeploymentOptionsEx::getOrder)));
    // 2.annotated Verticle beans todo: read @Ordered value
    // 2.1.SpringBootApplication(if it's a Verticle)
    Map<String, ? extends Verticle> map1 = Beans.withType(Verticle.class).withQualifier(SpringBootApplication.class).getBeanMap();
    // 2.2.AutoDeploy
    Map<String, ? extends Verticle> map2 = Beans.withType(Verticle.class).withQualifier(AutoDeploy.class).getBeanMap();
    // merge
    Map<String, Verticle> m = new HashMap<>();
    m.putAll(map1);
    m.putAll(map2);
    m.forEach((beanName, verticle) -> allDeploys.add(new VerticleDeploy().setDescriptor(beanName)));
    // deploy verticles in the list one by one
    int deployedCount = 0;
    // verticles with order=0
    @SuppressWarnings("rawtypes")
    List<Future> jobList = new ArrayList<>();
    for (VerticleDeploy vd : allDeploys) {
      if (vd.isEnabled()) {
        String descriptor = vd.getDescriptor();
        String jobName = "deploy verticle " + descriptor;
        if (descriptor.contains(":")) { // verticle descriptor
          EzJob<String> job = createJob(jobName).then(p -> vertx.deployVerticle(descriptor, vd, p));
          if (vd.getOrder() == 0) jobList.add(job.start().future());
          else job.join();
        } else { // bean name or class name
          Supplier<Verticle> provider = Beans.<Verticle>withDescriptor(
            descriptor
          ).withQualifier(
            vd.getBeanQualifier()
          ).getFirstProvider();
          EzJob<String> job = createJob(jobName).then(p -> vertx.deployVerticle(provider, vd, p));
          if (vd.getOrder() == 0) jobList.add(job.start().future());
          else job.join();
        }
        deployedCount++;
        log.debug("deployed verticle, descriptor: {}, qualifier: {}",
          vd.getDescriptor(), vd.getBeanQualifier());
      } else {
        log.debug("skip disabled verticleDeploy, descriptor: {}, qualifier: {}",
          vd.getDescriptor(), vd.getBeanQualifier());
      }
    }
    createJob("deploy verticles").thenSupply(() -> CompositeFuture.all(jobList)).join();
    return deployedCount;
  }

  private <T> EzJob<T> createJob(String jobName) {
    return EzJob.create(vertx, jobName);
  }

  @Override
  public boolean supportsEventType(@NonNull Class<? extends ApplicationEvent> eventType) {
    return ApplicationStartedEvent.class.isAssignableFrom(eventType);
  }

  @Override
  public int getOrder() {
    return vertxProps.getAutoDeployerOrder();
  }

  @Override
  public void onApplicationEvent(@NonNull ApplicationEvent event) {
    if (event instanceof ApplicationStartedEvent) {
      log.info("auto deploy start");
      int count = doDeploy();
      if (count < 1) {
        log.warn("auto deploy finish. no configured VerticleDeploy beans or @AutoDeploy annotated verticles");
      } else {
        log.info("auto deploy finish. {} verticle(s) deployed", count);
      }
    }
  }
}