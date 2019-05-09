package ez.spring.vertx;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public final class FutureEx<T> extends CompletableFuture<T> implements Handler<AsyncResult<T>> {
    public static <T> FutureEx<T> future() {
        return new FutureEx<>();
    }

    public static <T> FutureEx<T> succeedFuture(T result) {
        FutureEx<T> future = future();
        future.complete(result);
        return future;
    }

    public static <T> FutureEx<T> failedFuture(Throwable err) {
        FutureEx<T> future = future();
        future.completeExceptionally(err);
        return future;
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

    @Override
    public void handle(AsyncResult<T> event) {
        if (event.succeeded()) complete(event.result());
        else completeExceptionally(event.cause());
    }
}
