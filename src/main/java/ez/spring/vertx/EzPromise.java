package ez.spring.vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@SuppressWarnings("WeakerAccess")
public class EzPromise {
    public static <T> Promise<T> fromCompletableFuture(CompletableFuture<T> future) {
        Promise<T> promise = Promise.promise();
        future.whenComplete((value, err) -> {
            if (future.isCompletedExceptionally()) promise.fail(err);
            else promise.complete(value);
        });
        return promise;
    }

    /**
     * Warning: this method will set(overwrite) handler for future!
     *
     * @param future origin vertx future
     * @param <T>    future value type
     * @return wrapped jdk future
     * @throws CompletionException if future failed
     */
    public static <T> CompletableFuture toCompletableFuture(Future<T> future) throws CompletionException {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        future.setHandler(event -> {
            if (event.succeeded()) completableFuture.complete(event.result());
            else completableFuture.completeExceptionally(event.cause());
        });
        return completableFuture;
    }
}
