package org.wso2.carbon.analytics.eventsink.internal.queue;

import java.util.concurrent.*;

public class AnalyticsBlockingExecutor extends ThreadPoolExecutor {

    private final Semaphore semaphore;

    public AnalyticsBlockingExecutor(final int poolSize) {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        semaphore = new Semaphore(poolSize);
    }

    @Override
    public void execute(final Runnable task) {
        boolean acquired = false;
        do {
            try {
                semaphore.acquire();
                acquired = true;
            } catch (final InterruptedException e) {
                // Do nothing
            }
        } while (!acquired);
        super.execute(task);
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        super.afterExecute(r, t);
        semaphore.release();
    }
}
