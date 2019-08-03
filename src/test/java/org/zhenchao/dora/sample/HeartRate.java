package org.zhenchao.dora.sample;

public class HeartRate {

    private long timestamp;
    private int heartRate;

    public HeartRate(long timestamp, int heartRate) {
        this.timestamp = timestamp;
        this.heartRate = heartRate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getHeartRate() {
        return heartRate;
    }
}
