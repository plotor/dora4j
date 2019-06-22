package org.zhenchao.dora.schedule.timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhenchao.wang 2019-04-18 15:56
 * @version 1.0.0
 */
public class MockTime {

    private final long autoTickMs;

    // Values from `nanoTime` and `currentTimeMillis` are not comparable, so we store them separately to allow tests
    // using this class to detect bugs where this is incorrectly assumed to be true
    private final AtomicLong timeMs;
    private final AtomicLong highResTimeNs;

    public MockTime() {
        this(0, System.currentTimeMillis(), System.nanoTime());
    }

    public MockTime(long autoTickMs) {
        this(autoTickMs, System.currentTimeMillis(), System.nanoTime());
    }

    public MockTime(long autoTickMs, long currentTimeMs, long currentHighResTimeNs) {
        this.timeMs = new AtomicLong(currentTimeMs);
        this.highResTimeNs = new AtomicLong(currentHighResTimeNs);
        this.autoTickMs = autoTickMs;
    }

    public long milliseconds() {
        this.maybeSleep(autoTickMs);
        return timeMs.get();
    }

    public long nanoseconds() {
        this.maybeSleep(autoTickMs);
        return highResTimeNs.get();
    }

    public long hiResClockMs() {
        return TimeUnit.NANOSECONDS.toMillis(this.nanoseconds());
    }

    private void maybeSleep(long ms) {
        if (ms != 0) {
            this.sleep(ms);
        }
    }

    public void sleep(long ms) {
        timeMs.addAndGet(ms);
        highResTimeNs.addAndGet(TimeUnit.MILLISECONDS.toNanos(ms));
    }

}
