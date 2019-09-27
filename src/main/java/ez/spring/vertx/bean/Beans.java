package ez.spring.vertx.bean;

import ez.spring.vertx.util.EzUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class Beans<T> implements BeanGetterFirstStep<T> {
    public static final String DEFAULT_KEY = Beans.class.getCanonicalName() + ".DEFAULT_BEAN";
    private final ApplicationContext context;
    private final String descriptor;
    private final Class<T> beanType;
    private boolean isImplicitBeanAllowed = false;
    private String qualifier = null;
    private Class<? extends Annotation> qualifierClass = null;

    private Beans(ApplicationContext context, String descriptor) {
        this.context = Objects.requireNonNull(context);
        this.descriptor = Objects.requireNonNull(descriptor);
        beanType = null;
    }

    private Beans(ApplicationContext context, Class<T> beanType) {
        this.context = context;
        descriptor = null;
        this.beanType = beanType;
    }

    public static <T> BeanGetterFirstStep<T> withDescriptor(String descriptor) {
        return new Beans<>(EzUtil.getApplicationContext(), descriptor);
    }

    public static <T> BeanGetterFirstStep<T> withType(Class<T> beanType) {
        return new Beans<>(EzUtil.getApplicationContext(), beanType);
    }

    @Override
    public BeanGetterFinalStep<T> withQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    @Override
    public BeanGetterFinalStep<T> withQualifierType(Class<? extends Annotation> qualifierClass) {
        this.qualifierClass = qualifierClass;
        return this;
    }

    @Override
    public BeanGetterFinalStep<T> allowImplicit() {
        isImplicitBeanAllowed = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Supplier<Map<String, ? extends T>> getProvider() {
        String beanName = getBeanName();
        if (beanName != null) {
            return () -> Collections.singletonMap(beanName, (T) context.getBean(beanName));
        }
        Class<? extends T> beanType = getBeanType();
        Class<? extends Annotation> qualifierType = getQualifierType();
        if (beanType == null) throw new RuntimeException("bean not specified");
        if (qualifierType == null) {
            if (isImplicitBeanAllowed) {
                return () -> {
                    Map<String, ? extends T> beanMap = context.getBeansOfType(beanType);
                    if (beanMap.isEmpty()) {
                        try {
                            T bean = beanType.getConstructor().newInstance();
                            return Collections.singletonMap(DEFAULT_KEY, bean);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    } else return beanMap;
                };
            } else {
                return () -> context.getBeansOfType(beanType);
            }
        } else {
            return () -> {
                Map<String, ? extends T> m1 = context.getBeansOfType(beanType);
                Map<String, ? extends T> m2 = (Map<String, ? extends T>) context.getBeansWithAnnotation(qualifierType);
                m1.keySet().retainAll(m2.keySet());
                return m1;
            };
        }
    }

    @Nullable
    private String getBeanName() {
        if (descriptor == null) return null;
        if (context.containsBeanDefinition(descriptor)) return descriptor;
        return null;
    }

    @SuppressWarnings("unchecked")
    private <C> Class<? extends C> getType(@Nullable Class<C> type, @Nullable String name) {
        if (type != null) return type;
        if (name == null) return null;
        ClassLoader classLoader = Objects.requireNonNull(context.getClassLoader());
        try {
            return (Class<? extends C>) classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(name + " is not a bean name nor a valid class", e);
        }
    }

    @Nullable
    private Class<? extends T> getBeanType() {
        return getType(beanType, descriptor);
    }

    @Nullable
    private Class<? extends Annotation> getQualifierType() {
        return getType(qualifierClass, qualifier);
    }
}
