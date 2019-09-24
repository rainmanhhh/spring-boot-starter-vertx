package ez.spring.vertx.deploy

import ez.spring.vertx.Beans
import ez.spring.vertx.Main
import ez.spring.vertx.VertxProps
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.*
import java.util.function.Supplier

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
        // 1.VerticleDeploy configList
        val beans = Beans.withType(VerticleDeploy::class.java).getBeans()
        val configList: List<VerticleDeploy> = vertxProps.verticles
        val allDeploys = (beans + configList).toMutableList()
        allDeploys.sortWith(Comparator.comparingInt { obj: VerticleDeploy -> obj.order })
        // 2.annotated Verticle beans(non-ordered)
        // 2.1.SpringBootApplication(if it's a Verticle)
        val map1 = Beans.withType(Verticle::class.java).withQualifierType(SpringBootApplication::class.java).getBeanMap()
        // 2.2.AutoDeploy
        val map2 = Beans.withType(Verticle::class.java).withQualifierType(AutoDeploy::class.java).getBeanMap()
        // merge
        val m = map1 + map2
        val annotatedDeploys = m.map {
            VerticleDeploy().setDescriptor(it.key)
        }
        allDeploys += annotatedDeploys
        // deploy verticles in the list one by one
        var deployedCount = 0
        // verticles with order 0
        val jobList: MutableList<Job> = mutableListOf()
        for (vd in allDeploys) {
            if (vd.isEnabled) {
                val descriptor: String = vd.descriptor
                if (descriptor.contains(":")) { // verticle descriptor
                    if (vd.order == 0) jobList += deployJob(descriptor, vd)
                    else vertx.deployVerticleAwait(descriptor, vd)
                } else { // bean name or class name
                    val provider = Beans.withDescriptor<Verticle>(
                            descriptor
                    ).withQualifier(
                            vd.beanQualifier
                    ).getFirstProvider()
                    if (vd.order == 0) jobList += deployJob(provider, vd)
                    else vertx.deployVerticleAwait(provider, vd)
                }
                deployedCount++
                log.debug("deployed verticle, descriptor: {}, qualifier: {}",
                        vd.descriptor, vd.beanQualifier)
            } else {
                log.debug("skip disabled verticleDeploy, descriptor: {}, qualifier: {}",
                        vd.descriptor, vd.beanQualifier)
            }
        }
        jobList.joinAll()
        return deployedCount
    }

    private fun deployJob(descriptor: String, options: DeploymentOptions): Job {
        return GlobalScope.launch(vertx.dispatcher()) {
            vertx.deployVerticleAwait(descriptor, options)
        }
    }

    private fun deployJob(supplier: Supplier<Verticle>, options: DeploymentOptions): Job {
        return GlobalScope.launch(vertx.dispatcher()) {
            vertx.deployVerticleAwait(supplier, options)
        }
    }

    override fun run(vararg args: String) = runBlocking {
        log.info("auto deploy start")
        val count = deployVerticles()
        if (count < 1) {
            log.warn("auto deploy finish. no configured VerticleDeploy beans or @AutoDeploy annotated verticles")
        } else {
            log.info("auto deploy finish. {} verticle(s) deployed", count)
        }
    }
}