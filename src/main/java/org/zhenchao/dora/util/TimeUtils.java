package org.zhenchao.dora.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class TimeUtils {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DF_YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DF_YM = DateTimeFormatter.ofPattern("yyyyMM");
    public static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter TF_HMS = DateTimeFormatter.ofPattern("HHmmss");

    private TimeUtils() {
    }

    public static boolean isLocalDateTime(String dateTime, DateTimeFormatter pattern) {
        if (StringUtils.isBlank(dateTime) || null == pattern) {
            return false;
        }
        try {
            LocalDateTime.parse(dateTime, pattern);
            return true;
        } catch (Throwable e) {
            // ignore
        }
        return false;
    }

    public static boolean isLocalDate(String date, DateTimeFormatter pattern) {
        if (StringUtils.isBlank(date) || null == pattern) {
            return false;
        }
        try {
            LocalDate.parse(date, pattern);
            return true;
        } catch (Throwable e) {
            // ignore
        }
        return false;
    }

    /* millis <-> LocalDate */

    public static LocalDate millisToDate(long millis) {
        return millisToDate(millis, ZoneId.systemDefault());
    }

    public static LocalDate millisToDate(long millis, ZoneId zone) {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate();
    }

    public static long dateToMillis(LocalDate date) {
        return dateToMillis(date, ZoneId.systemDefault());
    }

    public static long dateToMillis(LocalDate date, ZoneId zone) {
        return date.atStartOfDay().atZone(zone).toInstant().toEpochMilli();
    }

    public static long dateToMillis(String date, DateTimeFormatter df) {
        return dateToMillis(date, df, ZoneId.systemDefault());
    }

    public static long dateToMillis(String date, DateTimeFormatter df, ZoneId zone) {
        return LocalDate.parse(date, df).atStartOfDay().atZone(zone).toInstant().toEpochMilli();
    }

    /* millis <-> LocalDateTime */

    public static LocalDateTime millisToDateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime millisToDateTime(long millis, ZoneId zone) {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime();
    }

    public static long dateTimeToMillis(String dateTime, DateTimeFormatter dtf) {
        return dateTimeToMillis(dateTime, dtf, ZoneId.systemDefault());
    }

    public static long dateTimeToMillis(String dateTime, DateTimeFormatter dtf, ZoneId zone) {
        return LocalDateTime.parse(dateTime, dtf).atZone(zone).toInstant().toEpochMilli();
    }

    public static long dateTimeToMillis(LocalDateTime dateTime) {
        return null != dateTime ? dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis();
    }

    public static long toMillis(String value) {
        if (isLocalDateTime(value, DTF)) {
            return dateTimeToMillis(value, DTF);
        } else if (isLocalDate(value, DF)) {
            return dateToMillis(value, DF);
        } else if (isLocalDate(value, DF_YMD)) {
            return dateToMillis(value, DF_YMD);
        } else {
            throw new IllegalArgumentException(value + " can't be cast to millis, unknown format: " + value);
        }
    }

    public static Optional<Long> optToMillis(String value) {
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }
        if (isLocalDateTime(value, DTF)) {
            return Optional.of(dateTimeToMillis(value, DTF));
        } else if (isLocalDate(value, DF)) {
            return Optional.of(dateToMillis(value, DF));
        } else if (isLocalDate(value, DF_YMD)) {
            return Optional.of(dateToMillis(value, DF_YMD));
        } else {
            return Optional.empty();
        }
    }

    public static boolean isValidFormat(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return isLocalDateTime(value, DTF)
                || isLocalDate(value, DF)
                || isLocalDate(value, DF_YMD);
    }

    public static boolean isInvalidFormat(String value) {
        return !isValidFormat(value);
    }

    public static LocalDateTime parseToDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (isLocalDateTime(value, DTF)) {
            return LocalDateTime.parse(value, DTF);
        } else if (isLocalDate(value, DF)) {
            LocalDate localDate = LocalDate.parse(value, DF);
            return localDate.atStartOfDay();
        } else if (isLocalDate(value, DF_YMD)) {
            LocalDate localDate = LocalDate.parse(value, DF_YMD);
            return localDate.atStartOfDay();
        } else {
            throw new IllegalArgumentException(value + " can't be cast to local date time, unknown format: " + value);
        }
    }

    public static LocalDate parseToDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (isLocalDateTime(value, DTF)) {
            return LocalDateTime.parse(value, DTF).toLocalDate();
        } else if (isLocalDate(value, DF)) {
            return LocalDate.parse(value, DF);
        } else if (isLocalDate(value, DF_YMD)) {
            return LocalDate.parse(value, DF_YMD);
        } else {
            throw new IllegalArgumentException(value + " can't be cast to local date, unknown format : " + value);
        }
    }

    public static boolean sleep(long timeout, TimeUnit unit) {
        final long start = monotonicMillis();
        final long intervals = unit.toMillis(timeout);
        if (intervals > 100L) {
            try {
                unit.sleep(timeout);
            } catch (InterruptedException e) {
                // just wake up early
                Thread.currentThread().interrupt();
                return true;
            }
        } else {
            while (monotonicMillis() - start < intervals) {
                // spin for a while
            }
        }
        return false;
    }

    public static long intervalDaysBetween(LocalDate inclusive, LocalDate exclusive) {
        return intervalBetween(inclusive, exclusive, ChronoUnit.DAYS);
    }

    public static long intervalMonthsBetween(LocalDate inclusive, LocalDate exclusive) {
        return intervalBetween(inclusive, exclusive, ChronoUnit.MONTHS);
    }

    public static long intervalBetween(LocalDate inclusive, LocalDate exclusive, ChronoUnit unit) {
        return inclusive.until(exclusive, unit);
    }

    public static long intervalBetween(LocalDateTime inclusive, LocalDateTime exclusive, ChronoUnit unit) {
        return inclusive.until(exclusive, unit);
    }


    public static long monotonicMillis() {
        return TimeUnit.NANOSECONDS.toMillis(nanoseconds());
    }

    public static long monotonicMicros() {
        return TimeUnit.NANOSECONDS.toMicros(nanoseconds());
    }

    public static long milliseconds() {
        return System.currentTimeMillis();
    }

    public static long nanoseconds() {
        return System.nanoTime();
    }

}