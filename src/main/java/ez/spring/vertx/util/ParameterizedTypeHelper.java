package ez.spring.vertx.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class ParameterizedTypeHelper {
    private final Type[] acturalTypeArguments;
    private final Class<?> parentType;

    private ParameterizedTypeHelper(Class<?> subType, Class<?> parentType) {
        Objects.requireNonNull(subType);
        this.parentType = Objects.requireNonNull(parentType);
        if (!parentType.isAssignableFrom(subType)) throw new IllegalArgumentException(
                subType.getCanonicalName() + " is not inherited from " + parentType.getCanonicalName()
        );
        Class<?> clazz = subType;
        while (clazz.getSuperclass() != parentType) {
            clazz = clazz.getSuperclass();
        }
        // clazz is direct child
        ParameterizedType genericParent = (ParameterizedType) clazz.getGenericSuperclass();
        acturalTypeArguments = genericParent.getActualTypeArguments();
    }

    public static ParameterizedTypeHelper of(Class<?> subType, Class<?> parentType) {
        return new ParameterizedTypeHelper(subType, parentType);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> get(int index) {
        String requestTypeName = acturalTypeArguments[index].getTypeName();
        ClassLoader classLoader = Objects.requireNonNull(parentType.getClassLoader());
        try {
            return (Class<T>) classLoader.loadClass(requestTypeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
