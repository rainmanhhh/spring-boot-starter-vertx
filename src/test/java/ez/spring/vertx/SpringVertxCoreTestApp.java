package ez.spring.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringVertxCoreTestApp extends AbstractVerticle {
    static String id = "";

    public static void main(String[] args) {
        SpringApplication.run(SpringVertxCoreTestApp.class);
    }

    @Main
    @Bean
    public Verticle mainVerticle() {
        return new SpringVertxCoreTestApp();
    }

    @Override
    public void start() {
        id = deploymentID();
    }

    @Bean
    public VertxProps vertxProps() {
        VertxProps vertxProps = new VertxProps();
        vertxProps
                .setMaxEventLoopExecuteTimeUnit(TimeUnit.MINUTES)
                .setMaxEventLoopExecuteTime(2)
                .setMaxWorkerExecuteTimeUnit(TimeUnit.MINUTES)
                .setMaxWorkerExecuteTime(2)
                .setBlockedThreadCheckIntervalUnit(TimeUnit.MINUTES)
                .setBlockedThreadCheckInterval(2)
        ;
        return vertxProps;
    }
}