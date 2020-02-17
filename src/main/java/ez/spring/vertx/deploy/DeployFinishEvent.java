package ez.spring.vertx.deploy;

import org.springframework.context.ApplicationEvent;

public class DeployFinishEvent extends ApplicationEvent {
  /**
   * count of deployed verticles
   */
  private final int count;
  /**
   * Create a new {@code ApplicationEvent}.
   *
   * @param source the object on which the event initially occurred or with
   *               which the event is associated (never {@code null})
   */
  public DeployFinishEvent(Object source, int count) {
    super(source);
    this.count = count;
  }

  public int getCount() {
    return count;
  }
}
