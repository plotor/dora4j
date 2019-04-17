package org.zhenchao.dora.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhenchao.dora.util.ThreadUtils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple scheduler impl based on {@link ScheduledThreadPoolExecutor}
 *
 * @author zhenchao.wang 2019-03-30 14:38
 * @version 1.0.0
 */
public class DefaultScheduler implements Scheduler {

    private static final Logger log = LoggerFactory.getLogger(DefaultScheduler.class);

    private int threads;
    private String threadNamePrefix = "default-scheduler-";
    private boolean daemon = true;

    public DefaultScheduler(int threads) {
        this.threads = threads;
    }

    public DefaultScheduler(int threads, boolean daemon) {
        this.threads = threads;
        this.daemon = daemon;
    }

    public DefaultScheduler(int threads, String threadNamePrefix) {
        this.threads = threads;
        this.threadNamePrefix = threadNamePrefix;
    }

    public DefaultScheduler(int threads, String threadNamePrefix, boolean daemon) {
        this.threads = threads;
        this.threadNamePrefix = threadNamePrefix;
        this.daemon = daemon;
    }

    private volatile ScheduledThreadPoolExecutor executor;
    private AtomicInteger schedulerThreadId = new AtomicInteger(0);

    @Override
    public void startup() {
        log.info("Initializing the default task scheduler.");
        synchronized (this) {
            if (this.isStarted()) {
                throw new IllegalStateException("The default scheduler has already been started!");
            }
            executor = new ScheduledThreadPoolExecutor(threads);
            executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            executor.setThreadFactory(runnable ->
                    ThreadUtils.newThread(threadNamePrefix + schedulerThreadId.getAndIncrement(), runnable, daemon));
        }
    }

    @Override
    public void shutdown() {
        log.info("Shutting down the default task scheduler.");
        // We use the local variable to avoid NullPointerException if another thread shuts down scheduler at same time.
        ScheduledThreadPoolExecutor cachedExecutor = executor;
        if (cachedExecutor != null) {
            synchronized (this) {
                cachedExecutor.shutdown();
                this.executor = null;
            }
            try {
                cachedExecutor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @Override
    public boolean isStarted() {
        return null != executor;
    }

    @Override
    public void schedule(final String name, final Runnable runnable, long delayMs, long periodMs) {
        log.info("Scheduling task [{}] with initial delay [{}ms] and period [{}ms].", name, delayMs, periodMs);
        synchronized (this) {
            this.ensureRunning();
            if (periodMs >= 0) {
                executor.scheduleAtFixedRate(runnable, delayMs, periodMs, TimeUnit.MILLISECONDS);
            } else {
                executor.schedule(runnable, delayMs, TimeUnit.MICROSECONDS);
            }
        }
    }

    private void ensureRunning() {
        if (!this.isStarted()) throw new IllegalStateException("default scheduler is not running.");
    }
}
