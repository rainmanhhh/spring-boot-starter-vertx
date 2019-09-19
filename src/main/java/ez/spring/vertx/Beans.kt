package ez.spring.vertx

import org.springframework.context.ApplicationContext
import java.util.*
import java.util.function.Supplier

/**
 * use to get beans. support "implicit prototype bean"(class with no-args constructor)
 */
object Beans {
    private val defaultKey = Main::class.java.canonicalName

    @JvmStatic
    fun <T> withDescriptor(descriptor: String): BeanGetterFirstStep<T> {
        return BeanGetter(descriptor)
    }

    @JvmStatic
    fun <T> withType(beanType: Class<T>): BeanGetterFirstStep<T> {
        return BeanGetter(beanType)
    }

    interface BeanGetterFinalStep<T> {
        fun get(): T {
            return getProvider().get().values.first()
        }

        fun getBeans(): Collection<T> {
            return getProvider().get().values
        }

        fun getProvider(): Supplier<Map<String, T>>

        fun getFirstProvider(): Supplier<T> {
            return Supplier { getProvider().get().values.first() }
        }
    }

    interface BeanGetterFirstStep<T> : BeanGetterFinalStep<T> {
        fun withQualifier(qualifier: String?): BeanGetterFinalStep<T>
        fun withQualifierType(qualifierClass: Class<out Annotation>?): BeanGetterFinalStep<T>
    }

    class BeanGetter<T> : BeanGetterFirstStep<T> {
        /**
         * could be bean name or bean class name
         */
        private val descriptor: String?
        private val beanType: Class<out T>?
        /**
         * could be string qualifier
         */
        var qualifier: String? = null
        var qualifierClass: Class<out Annotation?>? = null

        constructor(descriptor: String) {
            this.descriptor = Objects.requireNonNull(descriptor)
            beanType = null
        }

        constructor(beanType: Class<T>) {
            this.beanType = Objects.requireNonNull(beanType)
            descriptor = null
        }

        override fun withQualifier(qualifier: String?): BeanGetterFinalStep<T> {
            this.qualifier = qualifier
            return this
        }

        override fun withQualifierType(qualifierClass: Class<out Annotation>?): BeanGetterFinalStep<T> {
            this.qualifierClass = qualifierClass
            return this
        }

        private fun getBeanType(): Class<out T>? {
            return getType(beanType, descriptor)
        }

        private fun getQualifierType() = getType(qualifierClass, qualifier)

        @Suppress("UNCHECKED_CAST")
        private fun <C> getType(type: Class<C>?, name: String?): Class<C>? {
            val context = VertxConfiguration.getApplicationContext()!!
            if (type != null) return type
            if (name == null) return null
            val classLoader = Objects.requireNonNull(context.classLoader)!!
            try {
                return classLoader.loadClass(name) as Class<C>
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("$name is not a bean name nor a valid class", e)
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun getProvider(): Supplier<Map<String, T>> {
            val context: ApplicationContext = VertxConfiguration.getApplicationContext()
            val beanType = getBeanType()
            val qualifierType = getQualifierType()
            if (beanType == null) throw RuntimeException("bean not specified")
            return if (qualifierType == null) {
                Supplier {
                    val beanMap = context.getBeansOfType(beanType)
                    if (beanMap.isEmpty()) {
                        mapOf<String, T>(defaultKey to beanType.getConstructor().newInstance())
                    } else beanMap
                }
            } else {
                Supplier {
                    val m1 = context.getBeansOfType(beanType)
                    val m2 = context.getBeansWithAnnotation(qualifierType)
                    m1.keys.retainAll(m2.keys)
                    m1
                }
            }
        }
    }
}