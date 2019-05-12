package ez.spring.vertx;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

import lombok.Data;

@Data(staticConstructor = "ofType")
public class Beans<T> {
    @SuppressWarnings("unchecked")
    public static <T> T get(String descriptor, String qualifier) {
        ApplicationContext context = VertxConfiguration.getApplicationContext();
        if (context.containsBean(descriptor)) {
            return ((T) context.getBean(descriptor));
        } else {
            ClassLoader classLoader = Objects.requireNonNull(context.getClassLoader());
            final Class<? extends T> targetClass;
            try {
                targetClass = (Class<? extends T>) classLoader.loadClass(descriptor);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(descriptor + " is not a bean name nor a valid class", e);
            }
            T result;
            try {
                if (qualifier == null) {
                    result = context.getBean(targetClass);
                } else {
                    result = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                            context.getAutowireCapableBeanFactory(),
                            targetClass,
                            qualifier
                    );
                }
            } catch (NoSuchBeanDefinitionException beanNotFound) {
                try {
                    result = targetClass.getConstructor().newInstance();
                } catch (Exception e) {
                    e.addSuppressed(beanNotFound);
                    throw new RuntimeException(e);
                }
            }
            return result;
        }
    }

    public static <T> T get(String descriptor) {
        return get(descriptor, null);
    }

    public static <T> T get(Class<T> type, String qualifier) {
        ApplicationContext context = VertxConfiguration.getApplicationContext();
        if (qualifier == null) return context.getBean(type);
        else return BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                context.getAutowireCapableBeanFactory(),
                type,
                qualifier
        );
    }

    public static <T> T get(Class<T> type) {
        ApplicationContext context = VertxConfiguration.getApplicationContext();
        return context.getBean(type);
    }
}