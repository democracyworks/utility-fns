package utility_works.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BoundedExecutor extends ThreadPoolExecutor {

  private final Semaphore semaphore;

  public BoundedExecutor(int bound) {
    super(bound, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    semaphore = new Semaphore(bound, true);
  }

  public <T> Future<T> blockingSubmit(final Callable<T> task) throws InterruptedException {
    semaphore.acquire();
    return submit(task);
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);

    semaphore.release();
  }
}
