package ez.spring.vertx.util;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.springframework.lang.Nullable;

@SuppressWarnings("WeakerAccess")
public class EzUtil {
    /**
     * null-safe version standard {@link Object#toString()}
     *
     * @param o target
     * @return `class@hashcode` of the object
     */
    public static String toString(@Nullable Object o) {
        return o == null ? "null" : o.getClass().getCanonicalName() + "@" + Integer.toHexString(o.hashCode());
    }

    /**
     * get actual parameterized types from child class.<br>
     * eg. <pre>class Child extends Parent&lt;String, Integer&gt;</pre>,
     * result.get(0) will return String.class,
     * result.get(1) will return Integer.class
     *
     * @param parentClass generic parent class
     * @param childClass  child class which has actual parameterized types
     * @param <P>         parent type
     * @return actual parameterized types
     */
    public static <P> ParameterizedTypes<P> parameterizedTypes(Class<P> parentClass, Class<? extends P> childClass) {
        return ParameterizedTypes.of(parentClass, childClass);
    }

    /**
     * @return current thread context owner({@link Vertx}. null if current thread is not a vertx thread
     */
    @Nullable
    public static Vertx vertxOrNull() {
        Context context = Vertx.currentContext();
        return context == null ? null : context.owner();
    }

    /**
     * @return current thread context owner({@link Vertx}
     * @throws IllegalStateException if current thread is not a vertx thread
     */
    public static Vertx vertx() {
        Vertx v = vertxOrNull();
        if (v == null) throw new IllegalStateException("current thread is not a vertx thread");
        return v;
    }
}
