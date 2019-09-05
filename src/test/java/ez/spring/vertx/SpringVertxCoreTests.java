package ez.spring.vertx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringVertxCoreTests {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void start() {
        logger.info("tests start");
    }

    @Test
    public void contextLoad() {
        logger.info("context load");
    }

    @After
    public void end() {
        logger.info("tests end");
    }
}
