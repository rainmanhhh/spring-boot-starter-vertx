package ez.spring.vertx;

import io.vertx.core.VertxOptions;

public class VertxProps extends VertxOptions {
    /**
     * unit: seconds
     */
    private long clusterJoinTimeout = 30;
    /**
     * unit: seconds
     */
    private long mainVerticleDeployTimeout = 120;

    public long getClusterJoinTimeout() {
        return clusterJoinTimeout;
    }

    public void setClusterJoinTimeout(long clusterJoinTimeout) {
        this.clusterJoinTimeout = clusterJoinTimeout;
    }

    public long getMainVerticleDeployTimeout() {
        return mainVerticleDeployTimeout;
    }

    public void setMainVerticleDeployTimeout(long mainVerticleDeployTimeout) {
        this.mainVerticleDeployTimeout = mainVerticleDeployTimeout;
    }
}
