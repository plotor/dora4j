package org.zhenchao.dora.schedule;

import org.junit.Test;
import org.zhenchao.dora.util.ThreadUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author zhenchao.wang 2019-03-30 15:03
 * @version 1.0.0
 */
public class DefaultSchedulerTest {

    @Test
    public void schedule() throws Exception {
        Scheduler scheduler = new DefaultScheduler(ThreadUtils.availableProcessors());
        scheduler.startup();
        scheduler.schedule("test",
                () -> System.out.println("Thread-" + Thread.currentThread().getName() + " is running."), 3000, 1000);

        TimeUnit.SECONDS.sleep(10);
    }

}