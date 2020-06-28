package ez.spring.vertx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;

import io.vertx.core.AbstractVerticle;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
public class AutoDeployVerticle extends AbstractVerticle {
  static HashSet<TestBean> beanSet = new HashSet<>();
  static HashSet<String> idSet = new HashSet<>();
  private TestBean testBean;

  public TestBean getTestBean() {
    return testBean;
  }

  @Autowired
  public AutoDeployVerticle setTestBean(TestBean testBean) {
    this.testBean = testBean;
    return this;
  }

  @Override
  public void start() {
    beanSet.add(getTestBean());
    idSet.add(deploymentID());
  }
}
