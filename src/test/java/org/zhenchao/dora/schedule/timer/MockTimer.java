package org.zhenchao.dora.schedule.timer;

import org.zhenchao.dora.util.DateTimeUtils;

import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenchao.wang 2019-04-18 15:31
 * @version 1.0.0
 */
public class MockTimer implements Timer {

    private MockTime time = new MockTime();
    private PriorityQueue<TimerTaskNode> taskQueue = new PriorityQueue<TimerTaskNode>();

    @Override
    public void add(TimerTask timerTask) {
        if (timerTask.getDelayMs() <= 0) {
            timerTask.run();
        } else {
            taskQueue.add(new TimerTaskNode(timerTask, timerTask.getDelayMs() + System.currentTimeMillis()));
        }
    }

    @Override
    public boolean advanceClock(long timeoutMs) {
        DateTimeUtils.sleep(timeoutMs, TimeUnit.MILLISECONDS);
        boolean executed = false;
        long now = System.currentTimeMillis();

        while (!taskQueue.isEmpty() && now > taskQueue.peek().getExpirationMs()) {
            TimerTaskNode taskNode = taskQueue.poll();
            if (!taskNode.cancelled()) {
                TimerTask task = taskNode.getTimerTask();
                task.run();
                executed = true;
            }
        }

        return executed;
    }

    @Override
    public int size() {
        return taskQueue.size();
    }

    @Override
    public void shutdown() {

    }
}
