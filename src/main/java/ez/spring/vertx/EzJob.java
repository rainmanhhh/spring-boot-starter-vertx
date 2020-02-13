package ez.spring.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

@SuppressWarnings("UnusedReturnValue")
public class EzJob<F> {
  private static final Logger log = LoggerFactory.getLogger(EzJob.class);
  private static final ThreadLocal<Long> tlId = ThreadLocal.withInitial(() -> 0L);
  private final Vertx vertx;
  private final String id;
  private final String name;
  private final Promise<?> starter;
  private Future<F> future;

  private EzJob(Vertx vertx, String id, String name, Promise<?> starter, Future<F> future) {
    if (starter.future().isComplete())
      throw new IllegalStateException("job [" + name + "] already started");
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
    return new EzJob<>(vertx, idStr, jobName, starter, starter.future());
  }

  /**
   * add a step to job chain
   *
   * @param composer function receives last step result and returns next future
   * @param <R>      next future type
   * @return a new job object with added step
   */
  public <R> EzJob<R> thenCompose(Function<F, Future<R>> composer) {
    return new EzJob<>(vertx, id, name, starter, future.compose(composer));
  }

  /**
   * add a step to job chain
   *
   * @param mapper function receives last step result and returns next future value
   * @param <R>    next future type
   * @return a new job object with added step
   */
  public <R> EzJob<R> thenMap(Function<F, R> mapper) {
    return new EzJob<>(vertx, id, name, starter, future.map(mapper));
  }

  public <R> EzJob<R> thenSupply(Supplier<Future<R>> supplier) {
    return new EzJob<>(vertx, id, name, starter, future.compose(r -> supplier.get()));
  }

  /**
   * add a step to job chain
   *
   * @param action function receives last step result and promise(starter) for next step
   * @param <T>    next step promise(starter) type. eg: in `Vertx.clusteredVertx(options, p)` T is Vertx
   * @return a new job object with added step
   */
  public <T> EzJob<T> then(BiConsumer<F, Promise<T>> action) {
    return thenCompose((F f) -> {
      Promise<T> p = Promise.promise();
      action.accept(f, p);
      return p.future();
    });
  }

  public <T> EzJob<T> then(Consumer<Promise<T>> action) {
    return thenCompose((F f) -> {
      Promise<T> p = Promise.promise();
      action.accept(p);
      return p.future();
    });
  }

  /**
   * start job asynchronously
   *
   * @return the promise
   */
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

  /**
   * start job with timeout
   *
   * @param milliseconds wait time. less than or equals to 0 means never timeout
   * @return last step result
   */
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

  /**
   * start job and wait with timeout
   *
   * @param milliseconds wait time. less than or equals to 0 means never timeout
   * @return last step result
   * @throws CompletionException any step failed
   */
  public F join(long milliseconds) throws CompletionException {
    return EzPromise.toCompletableFuture(
      start(milliseconds).future()
    ).join();
  }

  /**
   * start job and wait without timeout
   *
   * @return last step result
   * @throws CompletionException any step failed
   */
  public F join() throws CompletionException {
    log.info("waiting sync job: [{}]", name);
    return EzPromise.toCompletableFuture(
      start().future()
    ).join();
  }
}