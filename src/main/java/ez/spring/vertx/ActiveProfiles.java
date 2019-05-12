package ez.spring.vertx;

import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * active profiles from {@link ApplicationContext#getEnvironment()} (ignore character cases)
 */
public class ActiveProfiles extends HashSet<String> {
    private static ActiveProfiles instance = null;

    private ActiveProfiles(Collection<String> profileStrValues) {
        super(profileStrValues);
    }

    static void createInstance(ApplicationContext applicationContext) {
        String[] profileStrArr = applicationContext.getEnvironment().getActiveProfiles();
        instance = new ActiveProfiles(Arrays.asList(profileStrArr));
    }

    private static String format(Object profileValue) {
        return profileValue == null ? null : profileValue.toString().toLowerCase();
    }

    public static ActiveProfiles getInstance() {
        if (instance == null)
            throw new NullPointerException("ActiveProfiles.instance not created yet");
        return instance;
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

    @Override
    public boolean contains(Object o) {
        return super.contains(format(o));
    }

    @Override
    public boolean add(String s) {
        return super.add(format(s));
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(format(o));
    }
}
