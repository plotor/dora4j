package org.zhenchao.dora.schedule.timer;

/**
 * 定时任务抽象类
 *
 * @author zhenchao.wang 2019-04-17 16:10
 * @version 1.0.0
 */
public abstract class TimerTask implements Runnable {

    /** 任务名 */
    private String name;

    /** 当前任务的延迟时长（单位：毫秒） */
    protected long delayMs;

    private TimerTaskNode taskNode;

    public TimerTask(String name) {
        this.name = name;
    }

    /**
     * 从时间格中移除当前任务
     */
    public synchronized void cancel() {
        if (null != taskNode) {
            taskNode.remove();
        }
        this.taskNode = null;
    }

    public synchronized TimerTask setTaskNode(TimerTaskNode taskNode) {
        // 如果对应的任务之前已经被添加过，则先移除之前的添加记录
        if (null != this.taskNode && this.taskNode != taskNode) {
            this.taskNode.remove();
        }
        this.taskNode = taskNode;
        return this;
    }

    public TimerTaskNode getTaskNode() {
        return taskNode;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public TimerTask setDelayMs(long delayMs) {
        this.delayMs = delayMs;
        return this;
    }

    public String getName() {
        return name;
    }
}
