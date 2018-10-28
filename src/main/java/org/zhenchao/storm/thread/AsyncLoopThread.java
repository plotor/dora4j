package org.zhenchao.storm.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhenchao.util.ThreadUtils;

/**
 * @author zhenchao.wang 2018-06-14 18:10
 * @version 1.0.0
 */
public class AsyncLoopThread implements SmartThread {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncLoopThread.class);

    private Thread thread;
    private RunnableCallback afn;

    public AsyncLoopThread(RunnableCallback afn) {
        this.init(afn, false, Thread.NORM_PRIORITY, true);
    }

    public AsyncLoopThread(RunnableCallback afn, boolean daemon, int priority, boolean start) {
        this.init(afn, daemon, priority, start);
    }

    public AsyncLoopThread(RunnableCallback afn, boolean daemon, RunnableCallback kill_fn, int priority, boolean start) {
        this.init(afn, daemon, kill_fn, priority, start);
    }

    public void init(RunnableCallback afn, boolean daemon, int priority, boolean start) {
        // 使用默认的 kill fn
        RunnableCallback kill_fn = new AsyncLoopDefaultKill();
        this.init(afn, daemon, kill_fn, priority, start);
    }

    /**
     * @param afn 异步线程函数
     * @param daemon 是否是守护线程
     * @param kill_fn 进程被 kill 时操作触发的线程函数
     * @param priority 线程优先级
     * @param start 是否启动
     */
    private void init(RunnableCallback afn, boolean daemon, RunnableCallback kill_fn, int priority, boolean start) {
        if (kill_fn == null) {
            // 如果没有设置，则默认创建一个
            kill_fn = new AsyncLoopDefaultKill();
        }

        // 采用 AsyncLoopRunnable 对于 afn 和 kfn 进行包装
        Runnable runnable = new AsyncLoopRunnable(afn, kill_fn);
        thread = new Thread(runnable);
        String threadName = afn.getThreadName();
        if (threadName == null) {
            // 以 afn 的 simpleName 作为线程名称
            threadName = afn.getClass().getSimpleName();
        }
        // 配置线程
        thread.setName(threadName);
        thread.setDaemon(daemon);
        thread.setPriority(priority);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error("UncaughtException", e);
                ThreadUtils.haltProcess(1);
            }
        });

        this.afn = afn;

        if (start) {
            // 启动线程
            thread.start();
        }
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void join() throws InterruptedException {
        thread.join();
    }

    @Override
    public void interrupt() {
        thread.interrupt();
    }

    @Override
    public Boolean isSleeping() {
        return ThreadUtils.isThreadWaiting(thread);
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public void cleanup() {
        afn.shutdown();
    }
}