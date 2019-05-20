package ez.spring.vertx;

import java.util.Collections;
import java.util.List;

import ez.spring.vertx.deploy.VerticleDeploy;
import io.vertx.core.VertxOptions;

public class VertxProps extends VertxOptions {
    /**
     * timeout of joining to the cluster.
     * unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
     * less than 0 means wait forever
     */
    private long clusterJoinTimeout = 30_000L;
    /**
     * timeout of deploy all configured verticles(in beans and config files).
     * unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
     * less than 0 means wait forever
     */
    private long deployTimeout = 180_000L;
    /**
     * verticles to deploy at vertx start(after main verticle deployed).
     *
     * @see VerticleDeploy
     */
    private List<VerticleDeploy> verticles = Collections.emptyList();

    /**
     * if {@link ActiveProfiles#isDev()}, use large value for
     * {@link #getMaxEventLoopExecuteTime()}, {@link #getMaxWorkerExecuteTime()}, {@link #getBlockedThreadCheckInterval()}
     * to avoid timeout
     */
    public VertxProps() {
        if (ActiveProfiles.getInstance().isDev()) {
            setMaxEventLoopExecuteTime(2_000_000_000_000_000L); // 2 million seconds
            setMaxWorkerExecuteTime(60_000_000_000_000_000L); // 60 million seconds
            setBlockedThreadCheckInterval(1_000_000_000L); // 1 million seconds
        }
    }

    public long getClusterJoinTimeout() {
        return clusterJoinTimeout;
    }

    public VertxProps setClusterJoinTimeout(long clusterJoinTimeout) {
        this.clusterJoinTimeout = clusterJoinTimeout;
        return this;
    }

    public long getDeployTimeout() {
        return deployTimeout;
    }

    public VertxProps setDeployTimeout(long deployTimeout) {
        this.deployTimeout = deployTimeout;
        return this;
    }

    public List<VerticleDeploy> getVerticles() {
        return verticles;
    }

    public VertxProps setVerticles(List<VerticleDeploy> verticles) {
        this.verticles = verticles;
        return this;
    }
}
