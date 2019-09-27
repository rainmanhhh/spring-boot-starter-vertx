package ez.spring.vertx;

import io.vertx.core.AbstractVerticle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
