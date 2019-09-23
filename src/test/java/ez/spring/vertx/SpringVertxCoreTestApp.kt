package ez.spring.vertx

import ez.spring.vertx.deploy.AutoDeploy
import io.vertx.core.AbstractVerticle
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit

@AutoDeploy
@SpringBootApplication
class SpringVertxCoreTestApp : AbstractVerticle() {
    override fun start() {
        id = deploymentID()
    }

    @Bean
    fun vertxProps(): VertxProps {
        return VertxProps().apply {
            maxEventLoopExecuteTimeUnit = TimeUnit.MINUTES
            maxEventLoopExecuteTime = 2
            maxWorkerExecuteTimeUnit = TimeUnit.MINUTES
            maxWorkerExecuteTime = 2
            blockedThreadCheckIntervalUnit = TimeUnit.MINUTES
            blockedThreadCheckInterval = 2
        }
    }

    companion object {
        internal var id = ""
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(SpringVertxCoreTestApp::class.java)
        }
    }
}