package org.zhenchao.dora.schedule.timer;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zhenchao.dora.util.TimeUtils;

/**
 * @author zhenchao.wang 2019-04-18 15:30
 * @version 1.0.0
 */
public class DefaultTimerTest {

    private Timer timer;

    @Before
    public void setUp() throws Exception {
        timer = new DefaultTimer("test", 1, 3, TimeUtils.monotonicMillis());
    }

    @After
    public void tearDown() throws Exception {
        timer.shutdown();
    }

    @Test
    public void alreadyExpiredTask() throws Exception {
        List<Integer> output = new ArrayList<Integer>();

        List<CountDownLatch> latches = new ArrayList<CountDownLatch>();
        for (int i = -5; i < 0; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            timer.add(new TestTask(i, i, latch, output));
            latches.add(latch);
        }

        timer.advanceClock(0);

        for (final CountDownLatch latch : latches.subList(0, 5)) {
            Assert.assertTrue("already expired tasks should run immediately", latch.await(3, TimeUnit.SECONDS));
        }

        Assert.assertEquals("output of already expired tasks", new HashSet<Integer>(Arrays.asList(-5, -4, -3, -2, -1)), new HashSet<Integer>(output));
    }

    @Test
    public void taskExpiration() throws Exception {
        List<Integer> output = new ArrayList<Integer>();

        List<TestTask> tasks = new ArrayList<TestTask>();
        List<Integer> ids = new ArrayList<Integer>();

        List<CountDownLatch> latches = new ArrayList<CountDownLatch>();
        for (int i = 0; i < 5; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            tasks.add(new TestTask(i, i, latch, output));
            ids.add(i);
            latches.add(latch);
        }
        for (int i = 10; i < 100; i++) {
            CountDownLatch latch = new CountDownLatch(2);
            tasks.add(new TestTask(i, i, latch, output));
            tasks.add(new TestTask(i, i, latch, output));
            ids.add(i);
            ids.add(i);
            latches.add(latch);
        }
        for (int i = 100; i < 500; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            tasks.add(new TestTask(i, i, latch, output));
            ids.add(i);
            latches.add(latch);
        }

        // randomly submit requests
        for (final TestTask task : tasks) {
            timer.add(task);
        }

        while (timer.advanceClock(2000)) {
        }

        for (final CountDownLatch latch : latches) {
            latch.await();
        }

        Collections.sort(ids);
        Assert.assertEquals("output should match", ids, output);
    }

    private class TestTask extends TimerTask {

        private int id;
        private CountDownLatch latch;
        private List<Integer> output;

        private AtomicBoolean completed = new AtomicBoolean(false);

        public TestTask(long delayMs, int id, CountDownLatch latch, List<Integer> output) {
            super("test-task");
            this.id = id;
            this.latch = latch;
            this.output = output;
            this.setDelayMs(delayMs);
        }

        @Override
        public void run() {
            if (completed.compareAndSet(false, true)) {
                synchronized (output) {
                    output.add(id);
                    System.out.println("Run job, id: " + id);
                }
                latch.countDown();
            }
        }
    }
}

