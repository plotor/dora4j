package org.zhenchao.dora.schedule.timer;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 时间轮
 *
 * @author zhenchao.wang 2019-04-17 17:21
 * @version 1.0.0
 */
public class TimingWheel {

    /** 一个时间格的时间跨度 */
    private long tickMs;

    /** 时间轮的格数 */
    private int wheelSize;

    /** 时间轮的创建时间 */
    private long startMs;

    /** 各层级时间轮中的任务总数 */
    private AtomicInteger taskCounter;

    /** 各层级时间轮共用的任务队列 */
    private DelayQueue<TimingGrid> queue;

    /**
     * 时间轮的时间跨度，
     * 当前时间轮仅处理 [currentTime, currentTime + interval] 之间的定时任务，超过该范围的任务需要添加到上层时间轮中
     */
    private long interval;

    /** 时间格数组 */
    private TimingGrid[] buckets;

    /** 时间轮指针，将时间轮划分为到期部分和未到期部分 */
    private long currentTime;

    /** 对于上层时间轮的引用 */
    private TimingWheel overflowWheel;

    public TimingWheel(long tickMs, int wheelSize, long startMs, AtomicInteger taskCounter, DelayQueue<TimingGrid> queue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.startMs = startMs;
        this.taskCounter = taskCounter;
        this.queue = queue;
        this.interval = tickMs * wheelSize;
        this.buckets = new TimingGrid[wheelSize];
        for (int i = 0; i < wheelSize; i++) {
            // 初始化时间轮
            buckets[i] = new TimingGrid(taskCounter);
        }
        this.currentTime = startMs - (startMs % tickMs);
    }

    /**
     * 往时间轮中添加任务
     *
     * @param taskNode
     * @return
     */
    public boolean add(TimerTaskNode taskNode) {
        // 任务被取消
        if (taskNode.cancelled()) {
            return false;
        }

        // 任务已经过期
        long expiration = taskNode.getExpirationMs();
        if (expiration < currentTime + tickMs) {
            return false;
        }

        // 待添加任务正好位于当前时间轮的处理区间内
        if (expiration < currentTime + interval) {
            long virtualId = expiration / tickMs;
            // 获取目标时间格
            TimingGrid bucket = buckets[(int) (virtualId % wheelSize)];
            bucket.add(taskNode);

            // 设置到期时间
            if (bucket.setExpiration(virtualId * tickMs)) {
                queue.offer(bucket);
            }
            return true;
        }

        // 超出当前时间轮的处理范围，将任务添加到上层时间轮中进行处理
        if (null == overflowWheel) this.addOverflowWheel();
        return overflowWheel.add(taskNode);
    }

    /**
     * 推动当前时间轮指针，同时也会尝试推动上层时间轮的指针
     *
     * @param timeMs
     */
    public void advanceClock(long timeMs) {
        if (timeMs >= currentTime + tickMs) {
            // 推动当前时间轮的指针
            this.currentTime = timeMs - (timeMs % this.tickMs);

            // 尝试推动上层时间轮的指针
            if (null != overflowWheel) {
                overflowWheel.advanceClock(currentTime);
            }
        }
    }

    /**
     * 添加上层时间轮，
     * 默认情况下上层时间轮的 tickMs 是当前时间轮的时间跨度
     */
    private synchronized void addOverflowWheel() {
        if (null == overflowWheel) {
            this.overflowWheel = new TimingWheel(interval, wheelSize, currentTime, taskCounter, queue);
        }
    }

    public long getTickMs() {
        return tickMs;
    }

    public int getWheelSize() {
        return wheelSize;
    }

    public long getStartMs() {
        return startMs;
    }

    public AtomicInteger getTaskCounter() {
        return taskCounter;
    }

    public long getInterval() {
        return interval;
    }

    public long getCurrentTime() {
        return currentTime;
    }
}
