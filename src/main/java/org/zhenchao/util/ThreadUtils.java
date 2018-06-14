package org.zhenchao.util;

/**
 * @author zhenchao.wang 2018-06-14 18:07
 * @version 1.0.0
 */
public class ThreadUtils {

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

}
