package ez.spring.vertx;

import java.util.Collections;
import java.util.List;

import io.vertx.core.VertxOptions;
import lombok.Data;

@Data
public class VertxProps extends VertxOptions {
    /**
     * timeout of joining to the cluster.
     * unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
     * less than 0 means wait forever
     */
    private long clusterJoinTimeout = 30_000L;
    /**
     * timeout of deploy all configured verticles(in beans & config files).
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
}
