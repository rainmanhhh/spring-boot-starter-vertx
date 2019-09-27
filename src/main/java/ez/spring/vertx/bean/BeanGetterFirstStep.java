package ez.spring.vertx.bean;

import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;

public interface BeanGetterFirstStep<T> extends BeanGetterFinalStep<T> {
    BeanGetterFinalStep<T> withQualifier(@Nullable String qualifier);

    BeanGetterFinalStep<T> withQualifierType(@Nullable Class<? extends Annotation> qualifierClass);
}
