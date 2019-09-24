package ez.spring.vertx

import ez.spring.vertx.util.EzUtil
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
        return BeanGetter(EzUtil.applicationContext, descriptor)
    }

    @JvmStatic
    fun <T> withType(beanType: Class<T>): BeanGetterFirstStep<T> {
        return BeanGetter(EzUtil.applicationContext, beanType)
    }

    interface BeanGetterFinalStep<T> {
        fun allowImplicit(): BeanGetterFinalStep<T>

        fun get(): T {
            return getProvider().get().values.first()
        }

        fun getBeans(): Collection<T> {
            return getBeanMap().values
        }

        fun getBeanMap(): Map<String, T> {
            return getProvider().get()
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
        override fun allowImplicit(): BeanGetterFinalStep<T> {
            isImplicitBeanAllowed = true
            return this
        }

        private val context: ApplicationContext
        /**
         * could be bean name or bean class name
         */
        private val descriptor: String?
        private val beanType: Class<out T>?
        private var isImplicitBeanAllowed = false
        /**
         * could be string qualifier
         */
        var qualifier: String? = null
        var qualifierClass: Class<out Annotation?>? = null

        constructor(context: ApplicationContext, descriptor: String) {
            this.context = context
            this.descriptor = Objects.requireNonNull(descriptor)
            beanType = null
        }

        constructor(context: ApplicationContext, beanType: Class<T>) {
            this.context = context
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

        private fun getBeanName(): String? {
            if (descriptor == null) return null
            return if (context.containsBeanDefinition(descriptor)) descriptor
            else null
        }

        private fun getBeanType(): Class<out T>? {
            return getType(beanType, descriptor)
        }

        private fun getQualifierType() = getType(qualifierClass, qualifier)

        @Suppress("UNCHECKED_CAST")
        private fun <C> getType(type: Class<C>?, name: String?): Class<C>? {
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
            val beanName = getBeanName()
            if (beanName != null) {
                return Supplier {
                    mapOf(beanName to context.getBean(beanName) as T)
                }
            }
            val beanType = getBeanType()
            val qualifierType = getQualifierType()
            if (beanType == null) throw RuntimeException("bean not specified")
            return if (qualifierType == null) {
                if (isImplicitBeanAllowed) Supplier {
                    val beanMap = context.getBeansOfType(beanType)
                    if (beanMap.isEmpty()) {
                        mapOf<String, T>(defaultKey to beanType.getConstructor().newInstance())
                    } else beanMap
                } else Supplier {
                    context.getBeansOfType(beanType)
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