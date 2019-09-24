package ez.spring.vertx

import ez.spring.vertx.deploy.AutoDeploy
import io.vertx.core.AbstractVerticle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@AutoDeploy
@Component
class SpringVertxCoreTestComponent : AbstractVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun start() {
        logger.info("verticle {} start", javaClass)
    }
}