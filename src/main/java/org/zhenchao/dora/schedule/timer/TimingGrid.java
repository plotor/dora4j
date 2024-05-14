package org.zhenchao.dora.schedule.timer;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.zhenchao.dora.util.TimeUtils;

/**
 * 时间格，对应时间轮中的一格
 *
 * @author zhenchao.wang 2019-04-17 16:08
 * @version 1.0.0
 */
public class TimingGrid implements Delayed {

    /** 任务总数计数器 */
    private AtomicInteger taskCounter;

    private TimerTaskNode root = new TimerTaskNode(null, -1);

    /** 当前时间格的过期时间 */
    private AtomicLong expiration = new AtomicLong(-1L);

    public TimingGrid(AtomicInteger taskCounter) {
        this.taskCounter = taskCounter;
        this.root.setPrev(root);
        this.root.setNext(root);
    }

    /**
     * 遍历时间格中的任务，对于未被取消的任务应用 consumer 函数
     *
     * @param consumer
     */
    public synchronized void foreach(Consumer<TimerTask> consumer) {
        TimerTaskNode task = root.getNext();
        while (task != root) {
            TimerTaskNode nextTask = task.getNext();
            if (!task.cancelled()) {
                consumer.accept(task.getTimerTask());
            }
            task = nextTask;
        }
    }

    /**
     * 添加定时任务到对应时间格中
     *
     * @param taskNode
     */
    public void add(TimerTaskNode taskNode) {
        boolean done = false;
        while (!done) {
            // 先尝试从时间格中移除该任务（如果之前有添加的话）
            taskNode.remove();

            synchronized (this) {
                synchronized (taskNode) {
                    if (null == taskNode.getGrid()) {
                        // 添加到时间格尾部
                        TimerTaskNode tail = root.getPrev();
                        taskNode.setNext(root);
                        taskNode.setPrev(tail);
                        taskNode.setGrid(this);
                        tail.setNext(taskNode);
                        root.setPrev(taskNode);
                        // 任务计数加 1
                        taskCounter.incrementAndGet();
                        done = true;
                    }
                }
            }
        }
    }

    /**
     * 从时间格中移除指定任务
     *
     * @param taskNode
     */
    public synchronized void remove(TimerTaskNode taskNode) {
        synchronized (taskNode) {
            if (taskNode.getGrid() == this) {
                taskNode.getNext().setPrev(taskNode.getPrev());
                taskNode.getPrev().setNext(taskNode.getNext());
                taskNode.setNext(null);
                taskNode.setPrev(null);
                taskNode.setGrid(null);
                // 任务计数减 1
                taskCounter.decrementAndGet();
            }
        }
    }

    /**
     * 从时间格中移除所有的任务，并对每个任务应用 consumer 函数
     *
     * @param consumer
     */
    public synchronized void flush(Consumer<TimerTaskNode> consumer) {
        TimerTaskNode head = root.getNext();
        while (head != root) {
            this.remove(head);
            consumer.accept(head);
            head = root.getNext();
        }
        expiration.set(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Math.max(this.getExpiration() - TimeUtils.monotonicMillis(), 0L), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed that) {
        TimingGrid other = (TimingGrid) that;
        return (int) (this.getExpiration() - other.getExpiration());
    }

    public long getExpiration() {
        return expiration.get();
    }

    public boolean setExpiration(long expiration) {
        return this.expiration.getAndSet(expiration) != expiration;
    }
}
