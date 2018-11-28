package org.zhenchao.dora.callback;

/**
 * Base Runnable/Callback function
 *
 * 对 {@link Runnable}/{@link Callback}/{@link Shutdownable} 的一个聚合，提供了基础实现
 *
 * @author zhenchao.wang 2018-06-14 18:05
 * @version 1.0.0
 */
public class RunnableCallback implements Runnable, Callback, Shutdownable {

    @Override
    public void run() {
    }

    /**
     * 当任务异常退出时执行该方法
     *
     * @param args
     * @param <T>
     * @return
     */
    @Override
    public <T> Object execute(T... args) {
        return null;
    }

    /**
     * 当任务被 shutdown 时回调执行
     */
    @Override
    public void shutdown() {
    }

    public void preRun() {
    }

    public void postRun() {
    }

    /**
     * 返回当前执行出现的异常信息，如果发生异常，会中断执行
     *
     * @return
     */
    public Exception error() {
        return null;
    }

    /**
     * 线程睡眠时间（单位：毫秒），默认为 0 表示不睡眠，如果设置为负数则表示只执行一次
     *
     * @return
     */
    public long sleepMillis() {
        return 0;
    }

    public String getThreadName() {
        return null;
    }

}
