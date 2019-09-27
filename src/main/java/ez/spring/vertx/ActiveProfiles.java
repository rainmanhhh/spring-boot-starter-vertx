package ez.spring.vertx;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * active profiles(ignore character cases)
 */
public class ActiveProfiles extends HashSet<String> {
    ActiveProfiles(Collection<String> values) {
        super(values.stream().map(String::toLowerCase).collect(Collectors.toSet()));
    }

    public boolean isDev() {
        return contains("dev") || contains("development");
    }

    public boolean isProd() {
        return contains("prod") || contains("production");
    }

    public boolean isTest() {
        return contains("test");
    }
}
