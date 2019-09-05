package ez.spring.vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class EzJob<F> {
    private final String name;
    private final Promise<?> promise;
    private Future<F> future;

    private EzJob(String name, Promise<?> promise, Future<F> future) {
        this.name = name;
        this.promise = promise;
        this.future = future;
    }

    public static <P> EzJob<P> create(String jobName) {
        Promise<P> promise = Promise.promise();
        return new EzJob<P>(jobName, promise, promise.future());
    }

    public <R> EzJob<R> addStep(Function<F, Future<R>> action) {
        return new EzJob<>(name, promise, future.compose(action));
    }

    public <T> EzJob<T> addStep(BiConsumer<F, Promise<T>> action) {
        return addStep((F f) -> {
            Promise<T> p = Promise.promise();
            action.accept(f, p);
            return p.future();
        });
    }

    public Future<F> start() {
        EzPromise.logger.info("start job [{}]", name);
        promise.complete();
        return future;
    }

    public Future<F> start(Vertx vertx, long milliseconds) {
        EzPromise.setTimeout(promise, vertx, milliseconds, name);
        return start();
    }

    public F startAndWait(Vertx vertx, long milliseconds) {
        start(vertx, milliseconds);
        //noinspection unchecked
        return (F) EzPromise.completableFuture(future).join();
    }

    public F startAndWait() {
        start();
        //noinspection unchecked
        return (F) EzPromise.completableFuture(future).join();
    }
}
