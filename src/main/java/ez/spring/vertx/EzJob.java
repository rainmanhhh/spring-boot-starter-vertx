package ez.spring.vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EzJob<F> {
    private static final Logger log = LoggerFactory.getLogger(EzJob.class);
    private static final ThreadLocal<Long> tlId = ThreadLocal.withInitial(() -> 0L);
    private final Vertx vertx;
    private final String id;
    private final String name;
    private final Promise<?> starter;
    private Future<F> future;

    private EzJob(Vertx vertx, String id, String name, Promise<?> starter, Future<F> future) {
        if (starter.future().isComplete()) throw new IllegalStateException("job [" + name + "] already started");
        this.vertx = vertx;
        this.id = id;
        this.name = name;
        this.starter = starter;
        this.future = future;
    }

    public static <P> EzJob<P> create(Vertx vertx, String jobName) {
        long id = tlId.get() + 1;
        tlId.set(id);
        Promise<P> starter = Promise.promise();
        String idStr = Thread.currentThread().getId() + "-" + id;
        return new EzJob<P>(vertx, idStr, jobName, starter, starter.future());
    }

    public <R> EzJob<R> addStep(Function<F, Future<R>> action) {
        return new EzJob<>(vertx, id, name, starter, future.compose(action));
    }

    public <T> EzJob<T> addStep(BiConsumer<F, Promise<T>> action) {
        return addStep((F f) -> {
            Promise<T> p = Promise.promise();
            action.accept(f, p);
            return p.future();
        });
    }

    public Promise<F> start() {
        log.info("job start [{}][{}]", id, name);
        Promise<F> p = Promise.promise();
        future.setHandler(r -> {
            if (!p.future().isComplete()) {
                if (r.succeeded()) {
                    log.info("job success: [{}][{}]", id, name);
                    p.complete(r.result());
                } else {
                    log.error("job failed: [{}][{}]", id, name, r.cause());
                    p.fail(r.cause());
                }
            }
        });
        starter.complete();
        return p;
    }

    public Promise<F> start(long milliseconds) {
        Promise<F> promise = start();
        if (milliseconds > 0) {
            vertx.setTimer(milliseconds, id -> {
                if (!promise.future().isComplete()) {
                    String msg = "job timeout: [" + id + "][" + name + "] (" + milliseconds + " ms)";
                    promise.fail(new TimeoutException(msg));
                }
            });
        }
        return promise;
    }

    public F startSyncWait(long milliseconds) throws CompletionException {
        //noinspection unchecked
        return (F) EzPromise.completableFuture(
                start(milliseconds).future()
        ).join();
    }

    public F startSyncWait() throws CompletionException {
        log.info("waiting sync job: [{}]", name);
        //noinspection unchecked
        return (F) EzPromise.completableFuture(
                start().future()
        ).join();
    }
}
