package ez.spring.vertx.deploy;

import org.springframework.context.ApplicationEvent;

public class AutoDeployFinishEvent extends ApplicationEvent {
  /**
   * count of deployed verticles
   */
  private final int count;

  /**
   * @param source the object on which the event initially occurred or with
   *               which the event is associated (never {@code null})
   * @param count  count of deployed verticles
   */
  public AutoDeployFinishEvent(AutoDeployer source, int count) {
    super(source);
    this.count = count;
  }

  public int getCount() {
    return count;
  }
}
