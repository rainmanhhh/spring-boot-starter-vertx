package ez.spring.vertx;

import ez.spring.vertx.deploy.AutoDeploy;
import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@AutoDeploy
@Component
public class SpringVertxCoreTestComponent extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void start() {
        logger.info("verticle {} start", getClass());
    }
}
