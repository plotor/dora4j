package org.zhenchao.dora.schedule.timer;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhenchao.wang 2019-04-17 18:04
 * @version 1.0.0
 */
public class TimingGridTest {

    @Test
    public void test() {
        AtomicInteger sharedCounter = new AtomicInteger(0);
        TimingGrid list1 = new TimingGrid(sharedCounter);
        TimingGrid list2 = new TimingGrid(sharedCounter);
        TimingGrid list3 = new TimingGrid(sharedCounter);

        final List<TestTask> tasks = new ArrayList<TestTask>();
        for (int i = 1; i <= 10; i++) {
            TestTask task = new TestTask(0L);
            list1.add(new TimerTaskNode(task, 10L));
            Assert.assertEquals(i, sharedCounter.get());
            tasks.add(task);
        }

        Assert.assertEquals(tasks.size(), sharedCounter.get());

        // reinserting the existing tasks shouldn't change the task count
        for (final TestTask task : tasks.subList(0, 4)) {
            int prevCount = sharedCounter.get();
            // new TimerTaskEntry(task) will remove the existing entry from the list
            list2.add(new TimerTaskNode(task, 10L));
            Assert.assertEquals(prevCount, sharedCounter.get());
        }

        Assert.assertEquals(10 - 4, this.size(list1));
        Assert.assertEquals(4, this.size(list2));

        Assert.assertEquals(tasks.size(), sharedCounter.get());

        // reinserting the existing tasks shouldn't change the task count
        for (final TestTask task : tasks.subList(4, tasks.size())) {
            int prevCount = sharedCounter.get();
            // new TimerTaskEntry(task) will remove the existing entry from the list
            list3.add(new TimerTaskNode(task, 10L));
            Assert.assertEquals(prevCount, sharedCounter.get());
        }
        Assert.assertEquals(0, this.size(list1));
        Assert.assertEquals(4, this.size(list2));
        Assert.assertEquals(6, this.size(list3));

        Assert.assertEquals(tasks.size(), sharedCounter.get());

        // cancel tasks in lists
        list1.foreach(TimerTask::cancel);
        Assert.assertEquals(0, this.size(list1));
        Assert.assertEquals(4, this.size(list2));
        Assert.assertEquals(6, this.size(list3));

        list2.foreach(TimerTask::cancel);
        Assert.assertEquals(0, this.size(list1));
        Assert.assertEquals(0, this.size(list2));
        Assert.assertEquals(6, this.size(list3));

        list3.foreach(TimerTask::cancel);
        Assert.assertEquals(0, this.size(list1));
        Assert.assertEquals(0, this.size(list2));
        Assert.assertEquals(0, this.size(list3));
    }

    private int size(TimingGrid list) {
        final AtomicInteger count = new AtomicInteger(0);
        list.foreach(timerTask -> count.incrementAndGet());
        return count.get();
    }

    private class TestTask extends TimerTask {

        public TestTask(long delayMs) {
            super("test-task");
            this.setDelayMs(delayMs);
        }

        @Override
        public void run() {

        }
    }
}

