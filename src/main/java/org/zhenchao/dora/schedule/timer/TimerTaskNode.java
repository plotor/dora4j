package org.zhenchao.dora.schedule.timer;

/**
 * @author zhenchao.wang 2019-04-17 16:10
 * @version 1.0.0
 */
public class TimerTaskNode implements Comparable<TimerTaskNode> {

    /** 封装的定时任务 */
    private TimerTask timerTask;

    /** 定时任务到期时间戳 */
    private long expirationMs;

    /** 定时任务所属的时间格 */
    private volatile TimingGrid grid;

    private TimerTaskNode next;
    private TimerTaskNode prev;

    public TimerTaskNode(TimerTask timerTask, long expirationMs) {
        this.timerTask = timerTask;
        if (null != timerTask) {
            timerTask.setTaskNode(this);
        }
        this.expirationMs = expirationMs;
    }

    /**
     * 判断任务是否被取消
     *
     * @return
     */
    public boolean cancelled() {
        return timerTask.getTaskNode() != this;
    }

    /**
     * 从对应时间格中移除当前任务
     */
    public void remove() {
        TimingGrid currentList = grid;
        while (null != currentList) {
            currentList.remove(this);
            currentList = grid;
        }
    }

    @Override
    public int compareTo(TimerTaskNode that) {
        return (int) (this.expirationMs - that.expirationMs);
    }

    public TimerTask getTimerTask() {
        return timerTask;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public TimingGrid getGrid() {
        return grid;
    }

    public TimerTaskNode setGrid(TimingGrid grid) {
        this.grid = grid;
        return this;
    }

    public TimerTaskNode getNext() {
        return next;
    }

    public TimerTaskNode setNext(TimerTaskNode next) {
        this.next = next;
        return this;
    }

    public TimerTaskNode getPrev() {
        return prev;
    }

    public TimerTaskNode setPrev(TimerTaskNode prev) {
        this.prev = prev;
        return this;
    }
}
