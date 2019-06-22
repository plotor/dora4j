package org.zhenchao.dora.util;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenchao.wang 2018-03-15 15:18
 * @since 1.0.1
 */
public class DateTimeUtils {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DF_YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DF_YM = DateTimeFormatter.ofPattern("yyyyMM");
    public static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter TF_HMS = DateTimeFormatter.ofPattern("HHmmss");

    private DateTimeUtils() {
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

    public static LocalDate millisToDate(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime millisToDateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static long dateTimeToMillis(String dateTime) {
        return dateTimeToMillis(dateTime, DTF);
    }

    public static long dateTimeToMillis(String dateTime, DateTimeFormatter dtf) {
        return StringUtils.isNotBlank(dateTime) ?
                LocalDateTime.parse(dateTime, dtf).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis();
    }

    public static long dateTimeToMillis(LocalDateTime dateTime) {
        return null != dateTime ? dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis();
    }

    public static long dateToMillis(String date) {
        return dateToMillis(date, DF_YMD);
    }

    public static long dateToMillis(String date, DateTimeFormatter df) {
        return StringUtils.isNotBlank(date) ?
                LocalDate.parse(date, df).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis();
    }

    public static long dateToMillis(LocalDate date) {
        return null != date ? date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis();
    }

    public static long toMillis(String value) {
        if (StringUtils.isBlank(value)) {
            return -1L;
        }
        if (isLocalDateTime(value, DTF)) {
            return dateTimeToMillis(value);
        } else if (isLocalDate(value, DF)) {
            return dateToMillis(value, DF);
        } else if (isLocalDate(value, DF_YMD)) {
            return dateToMillis(value, DF_YMD);
        } else {
            throw new IllegalArgumentException(value + " can't be cast to millis, unknown format : " + value);
        }
    }

    public static Optional<Long> optToMillis(String value) {
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }
        if (isLocalDateTime(value, DTF)) {
            return Optional.of(dateTimeToMillis(value));
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
            throw new IllegalArgumentException(value + " can't be cast to local date time, unknown format : " + value);
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

    public static void sleep(long timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            // ignore
        }
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

    public static long milliseconds() {
        return System.currentTimeMillis();
    }

    public static long hiResClockMs() {
        return TimeUnit.NANOSECONDS.toMillis(nanoseconds());
    }

    public static long nanoseconds() {
        return System.nanoTime();
    }
}