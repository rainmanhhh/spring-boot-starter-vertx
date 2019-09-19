package ez.spring.vertx.deploy

import ez.spring.vertx.Beans
import ez.spring.vertx.Main
import ez.spring.vertx.VertxProps
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import java.util.*

/**
 * auto deploy mainVerticle(bean of class [Verticle] with qualifier annotation [Main])
 * and verticles defined in application config file(prefix=vertx.verticles)
 */
class AutoDeployer(
        private val vertx: Vertx,
        private val vertxProps: VertxProps
) : CommandLineRunner {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private suspend fun deployVerticles(): Int {
        // merge VerticleDeploy beans & VerticleDeploy configList(sort by order)
        val beans = Beans.withType(VerticleDeploy::class.java).getBeans()
        val configList: List<VerticleDeploy> = vertxProps.verticles
        val allDeploys = (beans + configList).toMutableList()
        allDeploys.sortWith(Comparator.comparingInt { obj: VerticleDeploy -> obj.order })
        // annotated Verticle beans(non-ordered)
        val annotatedDeploys = Beans.withType(Verticle::class.java).withQualifierType(AutoDeploy::class.java).getBeans().map {
            VerticleDeploy().setDescriptor(it.javaClass.canonicalName)
        }
        allDeploys += annotatedDeploys
        // deploy verticles in the list one by one
        var deployedCount = 0
        for (verticleDeploy in allDeploys) {
            if (verticleDeploy.isEnabled) {
                val descriptor: String = verticleDeploy.descriptor
                if (descriptor.contains(":")) { // verticle descriptor
                    vertx.deployVerticleAwait(descriptor, verticleDeploy)
                } else { // bean name or class name
                    val provider = Beans.withDescriptor<Verticle>(
                            descriptor
                    ).withQualifier(
                            verticleDeploy.beanQualifier
                    ).getFirstProvider()
                    vertx.deployVerticleAwait(provider, verticleDeploy)
                }
                deployedCount++
                log.debug("deployed verticle, descriptor: {}, qualifier: {}",
                        verticleDeploy.descriptor, verticleDeploy.beanQualifier)
            } else {
                log.debug("skip disabled verticleDeploy, descriptor: {}, qualifier: {}",
                        verticleDeploy.descriptor, verticleDeploy.beanQualifier)
            }
        }
        return deployedCount
    }

    override fun run(vararg args: String) = runBlocking {
        val count = deployVerticles()
        if (count < 1) {
            log.warn("no enabled mainVerticle, no configured verticles")
        } else {
            log.info("auto deploy finish, {} verticle(s) deployed", count)
        }
    }
}