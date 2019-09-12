package ez.spring.vertx;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;

public class Beans {
    public interface BeanGetterFinalStep<T> {
        T get();

        Collection<? extends T> getBeans();
    }

    public interface BeanGetterFirstStep<T> extends BeanGetterFinalStep<T> {
        BeanGetterFinalStep<T> withQualifier(@Nullable String qualifier);

        BeanGetterFinalStep<T> withQualifierType(@Nullable Class<? extends Annotation> qualifierClass);
    }

    public static class BeanGetter<T> implements BeanGetterFirstStep<T> {
        final String descriptor;
        final Class<? extends T> beanType;
        String qualifier = null;
        Class<? extends Annotation> qualifierClass = null;

        BeanGetter(String descriptor) {
            this.descriptor = Objects.requireNonNull(descriptor);
            beanType = null;
        }

        BeanGetter(Class<T> beanType) {
            this.beanType = Objects.requireNonNull(beanType);
            descriptor = null;
        }

        @Override
        public BeanGetterFinalStep<T> withQualifier(@Nullable String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        @Override
        public BeanGetterFinalStep<T> withQualifierType(@Nullable Class<? extends Annotation> qualifierClass) {
            this.qualifierClass = qualifierClass;
            return this;
        }

        @SuppressWarnings("unchecked")
        private Class<? extends T> getBeanType(ApplicationContext context) {
            if (beanType != null) return beanType;
            ClassLoader classLoader = Objects.requireNonNull(context.getClassLoader());
            try {
                return (Class<? extends T>) classLoader.loadClass(descriptor);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(descriptor + " is not a bean name nor a valid class", e);
            }
        }

        @SuppressWarnings("unchecked")
        public Collection<? extends T> getBeans() {
            ApplicationContext context = VertxConfiguration.getApplicationContext();
            if (descriptor != null && context.containsBean(descriptor)) {
                return Collections.singleton((T) context.getBean(descriptor));
            } else {
                Class<? extends T> beanType = getBeanType(context);
                T result;
                try {
                    if (qualifier != null) {
                        result = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                                context.getAutowireCapableBeanFactory(),
                                beanType,
                                qualifier
                        );
                    } else if (qualifierClass != null) {
                        return getBeans(context, beanType, qualifierClass);
                    } else {
                        result = context.getBean(beanType);
                    }
                } catch (NoSuchBeanDefinitionException beanNotFound) {
                    if (qualifier == null && qualifierClass == null) {
                        try {
                            result = beanType.getConstructor().newInstance();
                        } catch (Throwable e) {
                            e.addSuppressed(beanNotFound);
                            throw new RuntimeException(e);
                        }
                    } else throw beanNotFound;

                }
                return Collections.singleton(result);
            }
        }

        @Override
        public T get() {
            Collection<? extends T> beans = getBeans();
            Iterator<? extends T> iterator = beans.iterator();
            if (iterator.hasNext()) return iterator.next();
            else {
                String qualifier = this.qualifier;
                if (qualifier == null && qualifierClass != null) qualifier = qualifierClass.getCanonicalName();
                throw new NoSuchBeanDefinitionException("bean not found! descriptor: " + descriptor + ", qualifier: " + qualifier);
            }
        }

        private Collection<? extends T> getBeans(ApplicationContext context, Class<? extends T> beanType, Class<? extends Annotation> qualifierClass) {
            Map<String, ? extends T> beansOfType = context.getBeansOfType(beanType);
            Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(qualifierClass);
            beansOfType.keySet().retainAll(beansWithAnnotation.keySet());
            return beansOfType.values();
        }
    }

    public static <T> BeanGetterFirstStep<T> withDescriptor(String descriptor) {
        return new BeanGetter<>(descriptor);
    }

    public static <T> BeanGetterFirstStep<T> withType(Class<T> beanType) {
        return new BeanGetter<>(beanType);
    }
}