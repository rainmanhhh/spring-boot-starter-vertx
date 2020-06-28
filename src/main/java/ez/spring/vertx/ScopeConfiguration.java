package ez.spring.vertx;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

import ez.spring.vertx.bean.Beans;

@Configuration
public class ScopeConfiguration {
  @Bean
  public CustomScopeConfigurer customScopeConfigurer() {
    CustomScopeConfigurer bean = new CustomScopeConfigurer();
    bean.addScope(Beans.SCOPE_THREAD, new SimpleThreadScope());
    return bean;
  }
}
