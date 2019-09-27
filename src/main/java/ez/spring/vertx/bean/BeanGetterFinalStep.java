package ez.spring.vertx.bean;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public interface BeanGetterFinalStep<T> {
    BeanGetterFinalStep<T> allowImplicit();

    Supplier<Map<String, ? extends T>> getProvider();

    default Supplier<T> getFirstProvider() {
        return () -> getProvider().get().values().iterator().next();
    }

    default Map<String, ? extends T> getBeanMap() {
        return getProvider().get();
    }

    default T get() {
        return getBeanMap().values().iterator().next();
    }

    default Collection<? extends T> getBeans() {
        return getBeanMap().values();
    }
}
