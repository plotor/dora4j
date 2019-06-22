package org.zhenchao.dora.schedule.timer;

/**
 * @author zhenchao.wang 2019-04-17 18:35
 * @version 1.0.0
 */
public interface Timer {

    /**
     * 添加定时任务，如果对应的任务到期则会提交执行
     *
     * @param timerTask 定时任务
     */
    void add(TimerTask timerTask);

    /**
     * 推动时间轮指针，对于到期的任务会提交执行
     *
     * @param timeoutMs 获取延时任务的超时时间
     * @return
     */
    boolean advanceClock(long timeoutMs);

    /**
     * 获取正在等待被执行的总任务数
     *
     * @return 任务数
     */
    int size();

    /**
     * 关闭时间轮，丢弃未执行的定时任务
     */
    void shutdown();

}
