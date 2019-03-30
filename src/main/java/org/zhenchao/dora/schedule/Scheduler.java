package org.zhenchao.dora.schedule;

/**
 * @author zhenchao.wang 2019-03-30 14:33
 * @version 1.0.0
 */
public interface Scheduler {

    /**
     * Initialize this scheduler so it is ready to accept scheduling of tasks
     */
    void startup();

    /**
     * Shutdown this scheduler. When this method is complete no more executions of background tasks will occur.
     * This includes tasks scheduled with a delayed execution.
     */
    void shutdown();

    /**
     * Check if the scheduler has been started
     *
     * @return
     */
    boolean isStarted();

    /**
     * Start to schedule a task.
     *
     * @param name The name of this task
     * @param runnable the schedule job
     * @param delayMs The amount of millis to wait before the first execution
     * @param periodMs The period millis with which to execute the task. If < 0 the task will execute only once.
     */
    void schedule(String name, Runnable runnable, long delayMs, long periodMs);

}
