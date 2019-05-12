package ez.spring.vertx;

import org.springframework.lang.Nullable;

public class EzUtil {
    /**
     * non-safe version standard {@link Object#toString()}
     */
    public static String toString(@Nullable Object o) {
        return o == null ? "null" : o.getClass().getCanonicalName() + "@" + Integer.toHexString(o.hashCode());
    }
}
