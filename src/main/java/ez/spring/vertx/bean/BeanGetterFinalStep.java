package ez.spring.vertx.bean;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public interface BeanGetterFinalStep<T> {
  /**
   * @return the provider of bean map
   */
  Supplier<Map<String, ? extends T>> getProvider();

  /**
   * @return the provider to get first bean which match conditions
   */
  default Supplier<T> getFirstProvider() {
    return () -> {
      Iterator<? extends T> iterator = getProvider().get().values().iterator();
      if (iterator.hasNext()) return iterator.next();
      else throw new BeanNotFoundException(this);
    };
  }

  /**
   * use the provider to get bean map
   *
   * @return bean map
   */
  default Map<String, ? extends T> getBeanMap() {
    return getProvider().get();
  }

  /**
   * @return the first bean in bean map
   */
  default T getBean() {
    Iterator<? extends T> iterator = getBeanMap().values().iterator();
    if (iterator.hasNext()) return iterator.next();
    else throw new BeanNotFoundException(this);
  }

  /**
   * @return value collection of bean map
   */
  default Collection<? extends T> getBeans() {
    return getBeanMap().values();
  }

  String getDescriptor();

  Class<? extends T> getBeanType();

  String getQualifierValue();

  Class<? extends Annotation> getQualifierClass();
}