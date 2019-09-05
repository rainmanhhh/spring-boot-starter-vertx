package ez.spring.vertx;

import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class EzPromise {
    public static Logger logger = LoggerFactory.getLogger(EzPromise.class);

    public static <T> Promise<T> promise(CompletableFuture<T> future) {
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
     */
    public static <T> CompletableFuture completableFuture(Future<T> future) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        future.setHandler(event -> {
            if (event.succeeded()) completableFuture.complete(event.result());
            else completableFuture.completeExceptionally(event.cause());
        });
        return completableFuture;
    }

    public static <T> Promise<T> setTimeout(Promise<T> promise, Vertx vertx, long milliseconds, String jobName) {
        if (milliseconds > 0) {
            vertx.setTimer(milliseconds, id -> {
                if (!promise.future().isComplete()) {
                    promise.fail("job[" + id + "] " + jobName + " timeout!");
                }
            });
        }
        return promise;
    }

    public static <T> Promise<T> setTimeout(Promise<T> promise, Vertx vertx, long milliseconds) {
        return setTimeout(promise, vertx, milliseconds, "");
    }
}
