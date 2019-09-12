package ez.spring.vertx;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * qualifier for `default` bean such as Verticle, HttpServer, SqlClient.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
public @interface Main {
}
