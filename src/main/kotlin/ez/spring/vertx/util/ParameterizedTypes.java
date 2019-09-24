package ez.spring.vertx.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * get actual parameterized types from child class.<br>
 * eg. <pre>class Child extends Parent&lt;String, Integer&gt;</pre>,
 * result.get(0) will return String.class,
 * result.get(1) will return Integer.class
 */
public class ParameterizedTypes<P> {
    private final Type[] actualTypeArguments;

    private ParameterizedTypes(Class<P> parentClass, Class<? extends P> childClass) {
        Objects.requireNonNull(parentClass);
        Objects.requireNonNull(childClass);
        if (!parentClass.isAssignableFrom(childClass)) throw new IllegalArgumentException(
                childClass.getCanonicalName() + " is not inherited from " + parentClass.getCanonicalName()
        );
        Class<?> clazz = childClass;
        while (clazz.getSuperclass() != parentClass) {
            clazz = clazz.getSuperclass();
        }
        // clazz is direct child
        ParameterizedType genericParent = (ParameterizedType) clazz.getGenericSuperclass();
        actualTypeArguments = genericParent.getActualTypeArguments();
    }

    public static <P> ParameterizedTypes<P> of(Class<P> parentType, Class<? extends P> subType) {
        return new ParameterizedTypes<>(parentType, subType);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> get(int index) {
        String requestTypeName = actualTypeArguments[index].getTypeName();
        ClassLoader classLoader = Objects.requireNonNull(EzUtil.getApplicationContext().getClassLoader());
        try {
            return (Class<T>) classLoader.loadClass(requestTypeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        return actualTypeArguments.length;
    }
}
