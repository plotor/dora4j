package org.zhenchao.dora.sample;

import org.zhenchao.dora.window.SlidingWindowCollection;
import org.zhenchao.dora.window.TimeWindow;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;

/**
 * In this sample, we calculate a sliding heart rate for a person. To simulate this, every 15 seconds a new heart rate
 * is added to a collection of HeartRates. Then, we transform the collection into an iterable set of TimeWindows to
 * calculate a sliding average over the duration of heart rates in the collection. Each window has a duration of 2 minutes
 * and a new window starts every minute. This means we should see roughly 4 heart rates per window with 2 heart rates
 * overlapping in each window (on either side of the window).
 */
public class Sample {

    private final static long ONE_MINUTE_MILLISECOND = 60000;
    private static StringBuilder builder = new StringBuilder();
    private static String prefix = "";

    public static void main(String[] args) {
        // arbitrary max min heart rate
        int max = 185;
        int min = 40;
        Random r = new Random();
        Person person = new Person(35);

        long time = System.currentTimeMillis();
        long end = time + (ONE_MINUTE_MILLISECOND * 10);
        for (; time < end; time += (ONE_MINUTE_MILLISECOND / 2)) {
            person.addHeartRate(r.nextInt((max - min) + 1) + min, time);

        }
        long start = System.currentTimeMillis();
        long every = ONE_MINUTE_MILLISECOND;
        long duration = ONE_MINUTE_MILLISECOND * 2;

        // transform the ArrayList into an iterable collection of sliding windows where
        // each window is 2 minutes long, and a new window starts every minute
        SlidingWindowCollection<HeartRate> swc = new SlidingWindowCollection<HeartRate>(
                person.getHeartRates(),
                HeartRate::getTimestamp,
                duration,
                every,
                start - 600000);

        // create and print a sliding average
        double slidingAverage = 0;
        int windowCount = 0;
        for (TimeWindow<HeartRate> window : swc) {
            printWindowInfo(windowCount, window);
            int hrSum = 0;
            for (HeartRate hr : window) {
                hrSum += hr.getHeartRate();
                printWindowContentInfo(hr);
            }

            slidingAverage += window.size() > 0 ? (hrSum / window.size()) : 0;
            windowCount++;
        }

        System.out.println("Sliding average: " + slidingAverage / windowCount);

    }

    public static void printWindowInfo(int count, TimeWindow<HeartRate> window) {
        builder.append(count > 0 ? "\t" : "");
        prefix = builder.toString();
        System.out.println(String.format("%s%s%s%s%s",
                prefix,
                "Start Time - ",
                Timestamp.from(Instant.ofEpochMilli(window.getStartTime())).toString(),
                " End Time - ",
                Timestamp.from(Instant.ofEpochMilli(window.getEndTime())).toString()));
    }

    public static void printWindowContentInfo(HeartRate hr) {
        System.out.println(String.format("%s%s%d%s%s",
                prefix,
                "HeartRate - ",
                hr.getHeartRate(),
                " Timestamp - ",
                Timestamp.from(Instant.ofEpochMilli(hr.getTimestamp())).toString()));
    }
}
