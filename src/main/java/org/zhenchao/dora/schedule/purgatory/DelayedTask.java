package org.zhenchao.dora.schedule.purgatory;

import org.zhenchao.dora.schedule.timer.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 延时任务定义
 *
 * @author zhenchao.wang 2019-04-18 17:34
 * @version 1.0.0
 */
public abstract class DelayedTask extends TimerTask {

    /** 标记任务是否已经执行完成 */
    private AtomicBoolean completed = new AtomicBoolean(false);

    /**
     * @param name 任务名
     * @param delayMs 当前任务延时时长（单位：毫秒）
     */
    public DelayedTask(String name, long delayMs) {
        super(name);
        this.delayMs = delayMs;
    }

    /**
     * 对于未完成的的延时任务，强制执行
     *
     * @return
     */
    public boolean forceComplete() {
        if (completed.compareAndSet(false, true)) {
            // 从时间轮中移除当前任务
            this.cancel();
            // 执行延时任务
            this.onComplete();
            return true;
        }
        return false;
    }

    /**
     * 检查当前延时任务是否已经执行完成
     *
     * @return
     */
    public boolean isCompleted() {
        return completed.get();
    }

    /**
     * 延时任务的具体逻辑，该方法仅允许被调用一次，且由 {@link #forceComplete()} 触发
     */
    public abstract void onComplete();

    /**
     * 尝试执行延时任务
     *
     * @return 是否执行完成
     */
    public abstract boolean tryComplete();

    /**
     * 尝试执行延时任务
     *
     * @return 是否执行完成
     */
    public synchronized boolean blockTryComplete() {
        return this.tryComplete();
    }

    /**
     * 如果延时任务未被强制执行，当任务到期时会触发该方法
     */
    public abstract void onExpiration();

    @Override
    public void run() {
        if (this.forceComplete()) {
            this.onExpiration();
        }
    }
}
