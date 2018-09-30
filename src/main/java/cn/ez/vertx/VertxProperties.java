package cn.ez.vertx;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("vertx")
public class VertxProperties {
    private boolean httpServerEnabled = false;

    public boolean isHttpServerEnabled() {
        return httpServerEnabled;
    }

    public void setHttpServerEnabled(boolean httpServerEnabled) {
        this.httpServerEnabled = httpServerEnabled;
    }
}
