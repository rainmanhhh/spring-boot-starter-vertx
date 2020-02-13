package ez.spring.vertx.http;

import io.vertx.core.http.HttpServerOptions;

public class MainHttpServerOptions extends HttpServerOptions {
  public static final int DEFAULT_PORT = 8999;
  /**
   * use spring ServerProperties.port as default value(if it's null or less than 0, fallback to {@link #DEFAULT_PORT})
   */
  private int port;

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public MainHttpServerOptions setPort(int port) {
    this.port = port;
    return this;
  }
}
