package ez.spring.vertx.util;

import org.springframework.lang.Nullable;

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
     * @param <P> parent type
     * @return actual parameterized types
     */
    public static <P> ParameterizedTypes<P> parameterizedTypes(Class<P> parentClass, Class<? extends P> childClass) {
        return ParameterizedTypes.of(parentClass, childClass);
    }
}
