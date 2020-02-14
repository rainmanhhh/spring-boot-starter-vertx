package ez.spring.vertx.util;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import ez.spring.vertx.ActiveProfiles;
import ez.spring.vertx.VertxConfiguration;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

@SuppressWarnings("WeakerAccess")
public class EzUtil {
  /**
   * null-safe version standard [Object.toString]
   *
   * @param o target
   * @return `class@hashcode` of the object
   */
  public static String toString(@Nullable Object o) {
    return o == null ? "null" : o.getClass().getCanonicalName() + "@" + System.identityHashCode(o);
  }

  /**
   * @return current thread context owner({@link Vertx}). null if current thread is not a vertx thread
   */
  @Nullable
  public static Vertx vertxOrNull() {
    Context context = Vertx.currentContext();
    return context == null ? null : context.owner();
  }

  /**
   * @return current thread context owner([Vertx]
   * @throws IllegalStateException if current thread is not a vertx thread
   */
  public static Vertx vertx() {
    Vertx vertx = vertxOrNull();
    if (vertx == null) throw new IllegalStateException("current thread is not a vertx thread");
    return vertx;
  }

  public static ApplicationContext getApplicationContext() {
    return VertxConfiguration.getApplicationContext();
  }

  public static ActiveProfiles getActiveProfiles() {
    return VertxConfiguration.getActiveProfiles();
  }
}