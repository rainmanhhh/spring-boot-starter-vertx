package ez.spring.vertx.bean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import ez.spring.vertx.util.EzUtil;

@SuppressWarnings("WeakerAccess")
public class Beans<T> implements BeanGetterFirstStep<T> {
  public static final String DEFAULT_KEY = Beans.class.getCanonicalName() + ".DEFAULT_BEAN";
  private final ApplicationContext context;
  private final String descriptor;
  private final Class<T> beanType;
  private String qualifierValue = null;
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

  /**
   * @param descriptor bean name or bean class name
   * @param <T>        bean type
   * @return BeanGetterFirstStep
   */
  public static <T> BeanGetterFirstStep<T> withDescriptor(String descriptor) {
    return new Beans<>(EzUtil.getApplicationContext(), descriptor);
  }

  public static <T> BeanGetterFirstStep<T> withType(Class<T> beanType) {
    return new Beans<>(EzUtil.getApplicationContext(), beanType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public BeanGetterFinalStep<T> withQualifier(@Nullable String qualifierValue) {
    this.qualifierValue = qualifierValue;
    if (qualifierValue != null) {
      ClassLoader classLoader = Objects.requireNonNull(context.getClassLoader());
      try {
        this.qualifierClass = (Class<? extends Annotation>) classLoader.loadClass(qualifierValue);
      } catch (ClassNotFoundException e) {
        this.qualifierClass = Qualifier.class;
      }
    }
    return this;
  }

  @Override
  public BeanGetterFinalStep<T> withQualifier(Class<? extends Annotation> qualifierClass) {
    this.qualifierClass = qualifierClass;
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
    if (beanType == null) throw new RuntimeException("bean not specified");
    if (qualifierClass == null) {
      return () -> context.getBeansOfType(beanType);
    } else if (qualifierClass == Qualifier.class) {
      return () -> {
        Map<String, ? extends T> m1 = context.getBeansOfType(beanType);
        Map<String, T> m2 = new HashMap<>(m1.size());
        m1.forEach((k, v) -> {
          Qualifier qualifier = v.getClass().getAnnotation(Qualifier.class);
          if (qualifier != null && qualifier.value().equals(qualifierValue)) {
            m2.put(k, v);
          }
        });
        return m2;
      };
    } else {
      return () -> {
        Map<String, ? extends T> m1 = context.getBeansOfType(beanType);
        Map<String, ? extends T> m2 = (Map<String, ? extends T>) context.getBeansWithAnnotation(qualifierClass);
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
  private <C> Class<? extends C> getType(@Nullable Class<C> type, @Nullable String orElseClassName) {
    if (type != null) return type;
    if (orElseClassName == null) return null;
    ClassLoader classLoader = Objects.requireNonNull(context.getClassLoader());
    try {
      return (Class<? extends C>) classLoader.loadClass(orElseClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(orElseClassName + " is not a bean name nor a valid class", e);
    }
  }

  /**
   * @return {@link #beanType} if not null; otherwise class whose name is {@link #descriptor}
   */
  @Nullable
  private Class<? extends T> getBeanType() {
    return getType(beanType, descriptor);
  }
}