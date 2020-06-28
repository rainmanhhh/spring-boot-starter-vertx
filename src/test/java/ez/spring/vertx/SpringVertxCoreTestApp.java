package ez.spring.vertx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ez.spring.vertx.deploy.DeployProps;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

@SpringBootApplication
public class SpringVertxCoreTestApp extends AbstractVerticle {
  static String id = "";

  public static void main(String[] args) {
    SpringApplication.run(SpringVertxCoreTestApp.class);
  }

  @Override
  public void start() {
    id = deploymentID();
  }
}
