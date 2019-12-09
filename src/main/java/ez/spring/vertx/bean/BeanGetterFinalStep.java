package ez.spring.vertx.bean;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public interface BeanGetterFinalStep<T> {
    Supplier<Map<String, ? extends T>> getProvider(boolean includeImplicit);

    default Supplier<Map<String, ? extends T>> getProvider() {
        return getProvider(false);
    }

    default Supplier<T> getFirstProvider(boolean includeImplicit) {
        return () -> getProvider(includeImplicit).get().values().iterator().next();
    }

    default Supplier<T> getFirstProvider() {
        return getFirstProvider(false);
    }

    default Map<String, ? extends T> getBeanMap(boolean includeImplicit) {
        return getProvider(includeImplicit).get();
    }

    default Map<String, ? extends T> getBeanMap() {
        return getBeanMap(false);
    }

    default T getBean(boolean includeImplicit) {
        return getBeanMap(includeImplicit).values().iterator().next();
    }

    default T getBean() {
        return getBean(false);
    }

    default Collection<? extends T> getBeans(boolean includeImplicit) {
        return getBeanMap(includeImplicit).values();
    }

    default Collection<? extends T> getBeans() {
        return getBeans(false);
    }
}