package ez.spring.vertx;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import ez.spring.vertx.deploy.DeployProps;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringVertxCoreTests {
  @Autowired
  private Vertx vertx;
  @Autowired
  private VertxProps vertxProps;
  @Autowired
  private ApplicationContext applicationContext;

  @Timed(millis = 3000)
  @Test
  public void undeployMainVerticle() {
    EzJob.create(vertx, "undeployMainVerticle").<Void>then(p ->
      vertx.undeploy(SpringVertxCoreTestApp.id, p)
    ).join();
  }

  @Timed(millis = 3000)
  @Test
  public void createTimeoutJob() {
    try {
      EzJob.create(vertx, "timeout job")
        .thenSupply(() -> Promise.promise().future())
        .setTimeout(200)
        .join();
    } catch (CompletionException e) {
      Assert.assertTrue(e.getCause() instanceof TimeoutException);
    }
  }

  @Timed(millis = 3000)
  @Test
  public void multiInstances() {
    DeployProps props = new DeployProps();
    int poolSize = vertxProps.getEventLoopPoolSize();
    int instanceCount = poolSize * 2;
    props.setEnabled(true)
      .setDescriptor(AutoDeployVerticle.class.getCanonicalName())
      .setInstances(instanceCount);
    EzJob.create(vertx, "deploy multi instances verticle")
      .thenSupply(() -> props.deploy(vertx)).join();
    Assert.assertEquals(AutoDeployVerticle.beanSet.size(), poolSize);
    Assert.assertEquals(AutoDeployVerticle.idSet.size(), instanceCount);
  }
}
