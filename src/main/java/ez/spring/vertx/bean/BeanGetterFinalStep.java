package ez.spring.vertx.bean;

import java.util.Collection;
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
    return () -> getProvider().get().values().iterator().next();
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
    return getBeanMap().values().iterator().next();
  }

  /**
   * @return value collection of bean map
   */
  default Collection<? extends T> getBeans() {
    return getBeanMap().values();
  }
}