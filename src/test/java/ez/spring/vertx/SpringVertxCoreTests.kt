package ez.spring.vertx

import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.kotlin.core.undeployAwait
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Timed
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeoutException

@RunWith(SpringRunner::class)
@SpringBootTest
class SpringVertxCoreTests {
    @Autowired
    lateinit var vertx: Vertx

    @Timed(millis = 5000L)
    @Test
    fun undeployMainVerticle() = runBlocking {
        vertx.undeployAwait(SpringVertxCoreTestApp.id)
    }

    @Timed(millis = 5000L)
    @Test
    fun createTimeoutJob() {
        try {
            EzJob.create<Any>(vertx, "timeout job")
                    .addStep { _ -> Promise.promise<Any>().future() }
                    .startSyncWait(200)
        } catch (e: CompletionException) {
            Assert.assertTrue(e.cause is TimeoutException)
        }
    }
}