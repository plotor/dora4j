package org.zhenchao.dora.schedule.purgatory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zhenchao.dora.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhenchao.wang 2019-06-22 12:27
 * @version 1.0.0
 */
public class DelayedTaskPurgatoryTest {

    private DelayedTaskPurgatory<MockDelayedTask> purgatory;

    @Before
    public void setUp() throws Exception {
        purgatory = new DelayedTaskPurgatory<>("mock");
    }

    @After
    public void tearDown() throws Exception {
        purgatory.shutdown();
    }

    @Test
    public void requestSatisfaction() {
        MockDelayedTask r1 = new MockDelayedTask(100000L);
        MockDelayedTask r2 = new MockDelayedTask(100000L);
        Assert.assertEquals("With no waiting requests, nothing should be satisfied", 0, purgatory.checkAndComplete("test1"));
        List<Object> watchKeys = new ArrayList<>();
        watchKeys.add("test1");
        Assert.assertFalse("r1 not satisfied and hence watched", purgatory.tryCompleteElseWatch(r1, watchKeys));
        Assert.assertEquals("Still nothing satisfied", 0, purgatory.checkAndComplete("test1"));
        watchKeys = new ArrayList<>();
        watchKeys.add("test2");
        Assert.assertFalse("r2 not satisfied and hence watched", purgatory.tryCompleteElseWatch(r2, watchKeys));
        Assert.assertEquals("Still nothing satisfied", 0, purgatory.checkAndComplete("test2"));
        r1.setCompletable(true);
        Assert.assertEquals("r1 satisfied", 1, purgatory.checkAndComplete("test1"));
        Assert.assertEquals("Nothing satisfied", 0, purgatory.checkAndComplete("test1"));
        r2.setCompletable(true);
        Assert.assertEquals("r2 satisfied", 1, purgatory.checkAndComplete("test2"));
        Assert.assertEquals("Nothing satisfied", 0, purgatory.checkAndComplete("test2"));
    }

    @Test
    public void requestExpiry() {
        long expiration = 20L;
        long start = DateTimeUtils.hiResClockMs();
        MockDelayedTask r1 = new MockDelayedTask(expiration);
        MockDelayedTask r2 = new MockDelayedTask(200000L);
        List<Object> watchKeys = new ArrayList<>();
        watchKeys.add("test1");
        Assert.assertFalse("r1 not satisfied and hence watched", purgatory.tryCompleteElseWatch(r1, watchKeys));
        watchKeys = new ArrayList<>();
        watchKeys.add("test2");
        Assert.assertFalse("r2 not satisfied and hence watched", purgatory.tryCompleteElseWatch(r2, watchKeys));
        r1.awaitExpiration();
        long elapsed = DateTimeUtils.hiResClockMs() - start;
        Assert.assertTrue("r1 completed due to expiration", r1.isCompleted());
        Assert.assertFalse("r2 hasn't completed", r2.isCompleted());
        Assert.assertTrue("Time for expiration elapsed should at least expiration", elapsed >= expiration);
    }

    @Test
    public void requestPurge() {
        MockDelayedTask r1 = new MockDelayedTask(100000L);
        MockDelayedTask r2 = new MockDelayedTask(100000L);
        MockDelayedTask r3 = new MockDelayedTask(100000L);
        List<Object> watchKeys = new ArrayList<Object>();
        watchKeys.add("test1");
        purgatory.tryCompleteElseWatch(r1, watchKeys);
        watchKeys.add("test2");
        purgatory.tryCompleteElseWatch(r2, watchKeys);
        watchKeys.add("test3");
        purgatory.tryCompleteElseWatch(r3, watchKeys);

        Assert.assertEquals("Purgatory should have 3 total delayed operations", 3, purgatory.delayed());
        Assert.assertEquals("Purgatory should have 6 watched elements", 6, purgatory.watched());

        // complete the operations, it should immediately be purged from the delayed operation
        r2.completable = true;
        r2.tryComplete();
        Assert.assertEquals("Purgatory should have 2 total delayed operations instead of " + purgatory.delayed(), 2, purgatory.delayed());

        r3.completable = true;
        r3.tryComplete();
        Assert.assertEquals("Purgatory should have 1 total delayed operations instead of " + purgatory.delayed(), 1, purgatory.delayed());

        // checking a watch should purge the watch list
        purgatory.checkAndComplete("test1");
        Assert.assertEquals("Purgatory should have 4 watched elements instead of " + purgatory.watched(), 4, purgatory.watched());

        purgatory.checkAndComplete("test2");
        Assert.assertEquals("Purgatory should have 2 watched elements instead of " + purgatory.watched(), 2, purgatory.watched());

        purgatory.checkAndComplete("test3");
        Assert.assertEquals("Purgatory should have 1 watched elements instead of " + purgatory.watched(), 1, purgatory.watched());
    }

    private class MockDelayedTask extends DelayedTask {

        private boolean completable = false;

        public MockDelayedTask(long delayMs) {
            super("mock-delayed-task", delayMs);
        }

        @Override
        public synchronized void onComplete() {
            this.notify();
        }

        @Override
        public boolean tryComplete() {
            return completable && this.forceComplete();
        }

        @Override
        public void onExpiration() {

        }

        public synchronized void awaitExpiration() {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        public boolean isCompletable() {
            return completable;
        }

        public MockDelayedTask setCompletable(boolean completable) {
            this.completable = completable;
            return this;
        }
    }
}