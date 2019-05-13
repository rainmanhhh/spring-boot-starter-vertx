package ez.spring.vertx.util;

import org.springframework.lang.Nullable;

public class EzUtil {
    /**
     * null-safe version standard {@link Object#toString()}
     * @param o target
     * @return `class@hashcode` of the object
     */
    public static String toString(@Nullable Object o) {
        return o == null ? "null" : o.getClass().getCanonicalName() + "@" + Integer.toHexString(o.hashCode());
    }

    public static <P> ParameterizedTypes<P> parameterizedTypes(Class<P> parentClass, Class<? extends P> childClass) {
        return ParameterizedTypes.of(parentClass, childClass);
    }
}
