package ez.spring.vertx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.vertx.core.AbstractVerticle;

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
