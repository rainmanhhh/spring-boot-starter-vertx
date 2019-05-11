package ez.spring.vertx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * an extend {@link CompletableFuture} which implements vertx {@link Handler}&lt;{@link AsyncResult}&gt;
 *
 * @param <T> The result type returned by this future's join and get methods
 */
public final class FutureEx<T> extends CompletableFuture<T> implements Handler<AsyncResult<T>> {
    public static <T> FutureEx<T> future() {
        return new FutureEx<>();
    }

    public static <T> FutureEx<T> succeededFuture(T result) {
        FutureEx<T> future = future();
        future.complete(result);
        return future;
    }

    public static <T> FutureEx<T> succeededFuture() {
        return succeededFuture(null);
    }

    public static <T> FutureEx<T> failedFuture(Throwable cause) {
        FutureEx<T> future = future();
        future.completeExceptionally(cause);
        return future;
    }

    public static <T> FutureEx<T> failedFuture(String message) {
        return failedFuture(new RuntimeException(message));
    }

    public static <T> FutureEx<T> future(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof FutureEx) {
            return (FutureEx<T>) completableFuture;
        } else {
            final FutureEx<T> future = future();
            completableFuture.thenApply(
                    future::complete
            ).exceptionally(
                    future::completeExceptionally
            );
            return future;
        }
    }

    /**
     * @param future  the future to wrap with timeout
     * @param vertx   {@link Vertx} instance
     * @param timeout unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}; invalid if equals or less then 0
     * @param jobName will be used in timeout error message. could be null
     * @param <T>     future result type
     * @return new future with timeout when param timeout is valid; otherwise original future
     */
    public static <T> CompletableFuture<T> setTimeout(
            CompletableFuture<T> future, Vertx vertx, long timeout, String jobName) {
        if (timeout > 0) {
            String message = "timeout(in ms): " + timeout;
            long timer = vertx.setTimer(timeout, event -> future.completeExceptionally(
                    new TimeoutException(jobName == null ? message : jobName + " " + message)
            ));
            return future.thenApply(t -> {
                vertx.cancelTimer(timer);
                return t;
            });
        } else return future;
    }

    /**
     * @param future  the future to wrap with timeout
     * @param vertx   {@link Vertx} instance
     * @param timeout unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}; invalid if equals or less then 0
     * @param <T>     future result type
     * @return new future with timeout when param timeout is valid; otherwise original future
     */
    public static <T> CompletableFuture<T> setTimeout(
            CompletableFuture<T> future, Vertx vertx, long timeout) {
        return setTimeout(future, vertx, timeout, null);
    }

    public boolean fail(Throwable cause) {
        return completeExceptionally(cause);
    }

    public boolean fail(String message) {
        return completeExceptionally(new RuntimeException(message));
    }

    /**
     * @param vertx   {@link Vertx} instance
     * @param timeout unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}; invalid if equals or less then 0
     * @param jobName will be used in timeout error message. could be null
     * @return new future with timeout when param timeout is valid; otherwise original future
     */
    public CompletableFuture<T> setTimeout(Vertx vertx, long timeout, String jobName) {
        return setTimeout(this, vertx, timeout, jobName);
    }

    /**
     * @param vertx   {@link Vertx} instance
     * @param timeout unit: {@link java.util.concurrent.TimeUnit#MILLISECONDS}; invalid if equals or less then 0
     * @return new future with timeout when param timeout is valid; otherwise original future
     */
    public CompletableFuture<T> setTimeout(Vertx vertx, long timeout) {
        return setTimeout(vertx, timeout, null);
    }

    @Override
    public void handle(AsyncResult<T> event) {
        if (event.succeeded()) complete(event.result());
        else completeExceptionally(event.cause());
    }
}
