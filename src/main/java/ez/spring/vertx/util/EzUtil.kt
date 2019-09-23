package ez.spring.vertx.util

import ez.spring.vertx.ActiveProfiles
import ez.spring.vertx.VertxConfiguration
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.context.ApplicationContext
import org.springframework.lang.Nullable

@Suppress("MemberVisibilityCanBePrivate")
object EzUtil {
    /**
     * null-safe version standard [Object.toString]
     *
     * @param o target
     * @return `class@hashcode` of the object
     */
    @JvmStatic
    fun toString(@Nullable o: Any?): String {
        return if (o == null) "null" else o.javaClass.canonicalName + "@" + Integer.toHexString(o.hashCode())
    }

    /**
     * get actual parameterized types from child class.<br></br>
     * eg. <pre>class Child extends Parent&lt;String, Integer&gt;</pre>,
     * result.get(0) will return String.class,
     * result.get(1) will return Integer.class
     *
     * @param parentClass generic parent class
     * @param childClass  child class which has actual parameterized types
     * @param <P>         parent type
     * @return actual parameterized types
    </P> */
    @JvmStatic
    fun <P> parameterizedTypes(parentClass: Class<P>, childClass: Class<out P>): ParameterizedTypes<P> {
        return ParameterizedTypes.of(parentClass, childClass)
    }

    /**
     * @return current thread context owner([Vertx]. null if current thread is not a vertx thread
     */
    @JvmStatic
    @Nullable
    fun vertxOrNull(): Vertx? {
        return Vertx.currentContext()?.owner()
    }

    /**
     * @return current thread context owner([Vertx]
     * @throws IllegalStateException if current thread is not a vertx thread
     */
    @JvmStatic
    fun vertx(): Vertx {
        return vertxOrNull() ?: throw IllegalStateException("current thread is not a vertx thread")
    }

    /**
     * create a coroutine scope with mdc(copy values from current thread)
     */
    fun mdcScope(): CoroutineScope = CoroutineScope(vertx().dispatcher() + MDCContext())

    @get:JvmStatic
    val applicationContext: ApplicationContext = VertxConfiguration.getApplicationContext()
    @get:JvmStatic
    val activeProfiles: ActiveProfiles = VertxConfiguration.getActiveProfiles()
}

@Suppress("EXPERIMENTAL_API_USAGE")
fun <T> Deferred<T>.toPromise(): Promise<T> {
    val promise = Promise.promise<T>()
    invokeOnCompletion {
        if (it == null) promise.complete(getCompleted())
        else promise.fail(it)
    }
    return promise
}