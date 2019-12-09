package ez.spring.vertx.bean;

import javax.annotation.Nullable;

import java.lang.annotation.Annotation;

public interface BeanGetterFirstStep<T> extends BeanGetterFinalStep<T> {
    /**
     * config qualifier and goto next step
     * @param qualifierValue qualifier class name or value of {@link org.springframework.beans.factory.annotation.Qualifier}<br>
     *                       note: null means do not use qualifier
     * @return {@link BeanGetterFinalStep}
     */
    BeanGetterFinalStep<T> withQualifier(@Nullable String qualifierValue);

    /**
     * config qualifier and goto next step
     * @param qualifierClass qualifier class<br>
     *                       note: null means do not use qualifier
     * @return {@link BeanGetterFinalStep}
     */
    BeanGetterFinalStep<T> withQualifier(@Nullable Class<? extends Annotation> qualifierClass);
}