package ez.spring.vertx.deploy;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * qualifier for auto deploy verticles. if you want to set deploy options, use VerticleDeploy bean instead.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
public @interface AutoDeploy {
}
