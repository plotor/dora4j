package org.zhenchao.dora.schedule.timer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhenchao.dora.util.ThreadUtils;
import org.zhenchao.dora.util.TimeUtils;

/**
 * @author zhenchao.wang 2019-04-17 18:41
 * @version 1.0.0
 */
public class DefaultTimer implements Timer {

    private static final Logger log = LoggerFactory.getLogger(DefaultTimer.class);

    private String executorName;

    /** 一个时间格的时间跨度，最小为 1 毫秒 */
    private long tickMs = 1L;
    private int wheelSize = 20;
    private long startMs = TimeUtils.monotonicMillis();

    /** 执行任务的线程池 */
    private ExecutorService taskExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return ThreadUtils.newThread("default-task-executor-" + executorName, runnable, false);
        }
    });

    /** 各层时间轮共用的队列 */
    private DelayQueue<TimingGrid> delayQueue = new DelayQueue<TimingGrid>();

    /** 各层时间轮共用任务个数计数器 */
    private AtomicInteger taskCounter = new AtomicInteger(0);

    /** 最底层时间轮 */
    private TimingWheel timingWheel = new TimingWheel(tickMs, wheelSize, startMs, taskCounter, delayQueue);

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    public DefaultTimer(String executorName) {
        this.executorName = executorName;
    }

    public DefaultTimer(String executorName, long tickMs, int wheelSize, long startMs) {
        this.executorName = executorName;
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.startMs = startMs;
    }

    @Override
    public void add(TimerTask timerTask) {
        readLock.lock();
        try {
            // 添加定时任务，如果任务已经到期但未被取消，则立即提交执行
            log.info("Add timer task, executor[{}], delay[{}ms]", executorName, timerTask.getDelayMs());
            this.addTimerTaskNode(new TimerTaskNode(timerTask, timerTask.getDelayMs() + TimeUtils.monotonicMillis()));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean advanceClock(long timeoutMs) {
        try {
            TimingGrid bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (null == bucket) {
                return false;
            }
            try {
                writeLock.lock();
                while (bucket != null) {
                    // 推进时间轮指针
                    log.info("Try to advance clock, executor[{}], expiration[{}ms]", executorName, bucket.getExpiration());
                    timingWheel.advanceClock(bucket.getExpiration());
                    /*
                     * 遍历处理当前时间格中的任务列表，提交执行到期但未被取消的任务，
                     * 对于未到期的任务重新添加到时间轮中继续等待被执行，期间可能会对任务在层级上执行降级
                     */
                    bucket.flush(DefaultTimer.this::addTimerTaskNode);
                    bucket = delayQueue.poll();
                }
            } finally {
                writeLock.unlock();
            }
            return true;
        } catch (InterruptedException e) {
            log.error("Get bucket error, executor[{}]", executorName, e);
            // ignore and return false
        }
        return false;
    }

    @Override
    public int size() {
        return taskCounter.get();
    }

    @Override
    public void shutdown() {
        taskExecutor.shutdown();
    }

    /**
     * 往时间轮中添加定时任务，同时检测添加的任务是否已经过期
     *
     * @param taskNode
     */
    private void addTimerTaskNode(TimerTaskNode taskNode) {
        if (!timingWheel.add(taskNode)) {
            // 对于过期但未被取消的任务，提交立即执行
            if (!taskNode.cancelled()) {
                taskExecutor.submit(taskNode.getTimerTask());
            }
        }
    }
}
