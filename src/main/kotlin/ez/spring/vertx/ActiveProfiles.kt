package ez.spring.vertx

import org.springframework.context.ApplicationContext

/**
 * active profiles from [ApplicationContext.getEnvironment] (ignore character cases)
 */
class ActiveProfiles private constructor(private val innerSet: Set<String>) : Set<String> by innerSet {
    constructor(profileStrValues: Collection<String?>) : this(profileStrValues.map {
        it.toString().toLowerCase()
    }.toSet())

    constructor() : this(emptyList())

    val isDev: Boolean
        get() = contains("dev") || contains("development")

    val isProd: Boolean
        get() = contains("prod") || contains("production")

    val isTest: Boolean
        get() = contains("test")
}