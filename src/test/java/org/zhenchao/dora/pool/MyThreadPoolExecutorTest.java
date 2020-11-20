package org.zhenchao.dora.pool;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenchao.wang 2020-11-19 18:35
 * @version 1.0.0
 */
public class MyThreadPoolExecutorTest {

    @Test
    @Ignore
    public void execute() throws Exception {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                8, 8,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(32),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        // 允许核心线程空闲时被回收
        executor.allowCoreThreadTimeOut(true);

        for (int i = 0; i < 40; i++) {
            executor.execute(() ->
                    System.out.println("thread-" + Thread.currentThread().getId() + " is running"));
        }

        int n = 0;
        while (n++ < 5) {
            System.out.println(executor.getPoolSize());
            TimeUnit.SECONDS.sleep(1);
        }

        for (int i = 0; i < 40; i++) {
            executor.execute(() ->
                    System.out.println("thread-" + Thread.currentThread().getId() + " is running"));
        }

        while (n++ < 10) {
            System.out.println(executor.getPoolSize());
            TimeUnit.SECONDS.sleep(1);
        }

    }

}