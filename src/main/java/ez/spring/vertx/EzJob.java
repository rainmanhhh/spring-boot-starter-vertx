package ez.spring.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
  private Future<?> future;
  private List<Future<?>> waitList = new ArrayList<>(1);
  private long timeout = -1;

  private EzJob(Vertx vertx, String id, String name, Promise<?> starter, Future<F> future) {
    if (starter.future().isComplete())
      throw new IllegalStateException("job [" + name + "] already started");
    this.vertx = vertx;
    this.id = id;
    this.name = name;
    this.starter = starter;
    this.future = future;
  }

  /**
   * create a job with timeout
   *
   * @param vertx   {@link Vertx} instance
   * @param jobName name of the job
   * @param timeout job timeout(valid only if value greater than 0)
   * @return job instance
   */
  public static EzJob<Void> create(Vertx vertx, String jobName, long timeout) {
    long id = tlId.get() + 1;
    tlId.set(id);
    Promise<Void> starter = Promise.promise();
    String idStr = Thread.currentThread().getId() + "-" + id;
    return new EzJob<>(vertx, idStr, jobName, starter, starter.future());
  }

  /**
   * create a job without timeout(can be set later)
   *
   * @param vertx   {@link Vertx} instance
   * @param jobName name of the job
   * @return job instance
   */
  public static EzJob<Void> create(Vertx vertx, String jobName) {
    return create(vertx, jobName, 0);
  }

  /**
   * @param timeout unit: milliseconds. valid only if value &gt; 0
   * @return this
   */
  public EzJob<F> setTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  public long getTimeout() {
    return timeout;
  }

  /**
   * add a step to job chain
   *
   * @param composer function receives last step result and returns next future
   * @param <R>      next future type
   * @return a new job object with added step
   */
  public <R> EzJob<R> thenCompose(Function<F, Future<R>> composer) {
    return setFuture(it -> it.compose(composer));
  }

//  public <R> EzJob<R> thenAsync()

  @SuppressWarnings("unchecked")
  private <R> EzJob<R> setFuture(Function<Future<F>, Future<R>> action) {
    future = action.apply((Future<F>) future);
    return (EzJob<R>) this;
  }

  @SuppressWarnings("unchecked")
  private <R> EzJob<R> setFuture(Supplier<Future<R>> supplier) {
    future = supplier.get();
    return (EzJob<R>) this;
  }

  /**
   * add a step to job chain
   *
   * @param mapper function receives last step result and returns next future value
   * @param <R>    next future type
   * @return a new job object with added step
   */
  public <R> EzJob<R> thenMap(Function<F, R> mapper) {
    return setFuture(it -> it.map(mapper));
  }

  public EzJob<Void> thenConsume(Consumer<F> consumer) {
    return setFuture(it -> it.compose(r -> {
      consumer.accept(r);
      return Future.succeededFuture();
    }));
  }

  public <R> EzJob<R> thenSupply(Supplier<Future<R>> supplier) {
    return setFuture(it -> it.compose(r -> supplier.get()));
  }

  /**
   * add a step to job chain
   *
   * @param action function receives last step result and promise(starter) for next step
   * @param <T>    next step promise(starter) type. eg: in `Vertx.clusteredVertx(options, p)` T is Vertx
   * @return job chain
   */
  public <T> EzJob<T> then(BiConsumer<F, Promise<T>> action) {
    return thenCompose((F f) -> {
      Promise<T> p = Promise.promise();
      action.accept(f, p);
      return p.future();
    });
  }

  /**
   * @param action function determine when to start next step and do not care about last step result
   * @param <T>    next step input param type
   * @return job chain
   */
  public <T> EzJob<T> then(Consumer<Promise<T>> action) {
    return setFuture(() -> {
      Promise<T> p = Promise.promise();
      action.accept(p);
      return p.future();
    });
  }

  public EzJob<F> then(Promise<F> promise) {
    return thenCompose((F f) -> {
      promise.complete(f);
      return promise.future();
    });
  }

  @SuppressWarnings("unchecked")
  private Future<F> getFuture() {
    return ((Future<F>) future);
  }

  /**
   * start job asynchronously
   *
   * @return the promise
   */
  private Promise<F> doJob() {
    log.info("job start [{}][{}]", id, name);
    Promise<F> p = Promise.promise();
    getFuture().onComplete(r -> {
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
   * start job asynchronously. <br>
   * if timeout &gt; 0, setup a timeout handler in vertx
   *
   * @return last step result
   */
  public Promise<F> start() {
    Promise<F> promise = doJob();
    long timeout = getTimeout();
    if (timeout > 0) {
      vertx.setTimer(timeout, id -> {
        if (!promise.future().isComplete()) {
          String msg = "job timeout: [" + id + "][" + name + "] (" + timeout + " ms)";
          promise.fail(new TimeoutException(msg));
        }
      });
    }
    return promise;
  }

  public Promise<Void> start(Promise<Void> finishHandler) {
    start().future().onComplete(it -> {
      if (!finishHandler.future().isComplete()) {
        if (it.succeeded()) finishHandler.complete();
        else finishHandler.fail(it.cause());
      }
    });
    return finishHandler;
  }

  /**
   * start job and wait synchronously. <br>
   * if timeout &gt; 0, setup a timeout handler in vertx
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