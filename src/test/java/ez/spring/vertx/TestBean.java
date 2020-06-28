package ez.spring.vertx;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ez.spring.vertx.bean.Beans;

@Lazy
@Scope(Beans.SCOPE_THREAD)
@Component
public class TestBean {
}
