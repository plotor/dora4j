package org.zhenchao.dora.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhenchao.wang 2018-06-14 18:07
 * @version 1.0.0
 */
public class ThreadUtils {

    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

    public static void haltProcess(int val) {
        Runtime.getRuntime().halt(val);
    }

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static boolean isThreadWaiting(Thread t) {
        // TODO by zhenchao 2018-06-14 18:19:18
        return false;
    }

    /**
     * Create a new thread
     *
     * @param name The name of the thread
     * @param runnable The work for the thread to do
     * @param daemon Should the thread block JVM shutdown?
     * @return The unstarted thread
     */
    public static Thread newThread(String name, Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemon);
        thread.setUncaughtExceptionHandler(
                (t, e) -> log.error("Uncaught exception in thread '" + t.getName(), e));
        return thread;
    }

    /**
     * Create a daemon thread
     *
     * @param name The name of the thread
     * @param runnable The runnable to execute in the background
     * @return The unstarted thread
     */
    public static Thread daemonThread(String name, Runnable runnable) {
        return newThread(name, runnable, true);
    }

    public static int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

}
