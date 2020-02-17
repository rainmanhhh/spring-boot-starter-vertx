package ez.spring.vertx.http;

import io.vertx.core.http.HttpServerOptions;

public class MainHttpServerOptions extends HttpServerOptions {
  /**
   * use ${server.port} if it equals or greater than 0
   */
  private int port = 8999;

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