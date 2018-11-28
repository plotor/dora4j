package org.zhenchao.dora.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhenchao.dora.support.IFunction;
import org.zhenchao.dora.support.ISupplier;

import java.util.concurrent.TimeUnit;

/**
 * @author zhenchao.wang 2018-07-19 11:00
 * @version 1.0.0
 */
public class RetryUtils {

    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    public static final int DEFAULT_RETRIES = 3;

    private RetryUtils() {
    }

    public static <T, R> R active(IFunction<T, ResultWrapper<R>> function) {
        return active(null, function, DEFAULT_RETRIES, 0);
    }

    public static <T, R> R active(IFunction<T, ResultWrapper<R>> function, int count) {
        return active(null, function, count, 0);
    }

    public static <T, R> R active(IFunction<T, ResultWrapper<R>> function, long intervalSeconds) {
        return active(null, function, DEFAULT_RETRIES, intervalSeconds);
    }

    public static <T, R> R active(T arg, IFunction<T, ResultWrapper<R>> function) {
        return active(arg, function, DEFAULT_RETRIES, 0);
    }

    public static <T, R> R active(T arg, IFunction<T, ResultWrapper<R>> function, int count) {
        return active(arg, function, count, 0);
    }

    public static <T, R> R active(T arg, IFunction<T, ResultWrapper<R>> function, long intervalSeconds) {
        return active(arg, function, DEFAULT_RETRIES, intervalSeconds);
    }

    public static <T, R> R active(T arg, IFunction<T, ResultWrapper<R>> function, int count, long intervalSeconds) {
        for (int i = count; i > 0; i--) {
            ResultWrapper<R> result = function.apply(arg);
            if (result.isNotRetry()) {
                log.info("Operation success and skip retry, retryCount[{}]", count - i);
                return result.getObj();
            }
            if (i == 1) {
                log.warn("Operation still failed and give up retry[{}]", i);
                if (null != result.getObj()) {
                    return result.getObj();
                }
                break;
            }
            if (intervalSeconds > 0) {
                try {
                    log.warn("Operation maybe failed and retry[{}], interval[{}s]", i, intervalSeconds);
                    TimeUnit.SECONDS.sleep(intervalSeconds);
                } catch (InterruptedException e) {
                    // ignore
                }
            } else {
                log.warn("Operation maybe failed and retry[{}]", i);
            }
        }
        return null;
    }

    public static <R> R active(ISupplier<ResultWrapper<R>> supplier) {
        return active(supplier, DEFAULT_RETRIES, 0);
    }

    public static <R> R active(ISupplier<ResultWrapper<R>> supplier, int count) {
        return active(supplier, count, 0);
    }

    public static <R> R active(ISupplier<ResultWrapper<R>> supplier, long intervalSeconds) {
        return active(supplier, DEFAULT_RETRIES, intervalSeconds);
    }

    public static <R> R active(ISupplier<ResultWrapper<R>> supplier, int count, long intervalSeconds) {
        for (int i = count; i > 0; i--) {
            ResultWrapper<R> result = supplier.get();
            if (result.isNotRetry()) {
                log.info("Operation success and skip retry, retryCount[{}]", count - i);
                return result.getObj();
            }
            if (i == 1) {
                log.warn("Operation still failed and give up retry[{}]", i);
                if (null != result.getObj()) {
                    return result.getObj();
                }
                break;
            }
            if (intervalSeconds > 0) {
                try {
                    log.warn("Operation maybe failed and retry[{}], interval[{}s]", i, intervalSeconds);
                    TimeUnit.SECONDS.sleep(intervalSeconds);
                } catch (InterruptedException e) {
                    // ignore
                }
            } else {
                log.warn("Operation maybe failed and retry[{}]", i);
            }
        }
        return null;
    }

    public static <T> ResultWrapper<T> retry() {
        return new ResultWrapper<T>(null, true);
    }

    public static <T> ResultWrapper<T> retry(T obj) {
        return new ResultWrapper<T>(obj, true);
    }

    public static <T> ResultWrapper<T> success() {
        return success(null);
    }

    public static <T> ResultWrapper<T> success(T obj) {
        return new ResultWrapper<T>(obj, false);
    }

    public static class ResultWrapper<T> {

        private T obj;
        private boolean retry;

        ResultWrapper(T obj, boolean retry) {
            this.obj = obj;
            this.retry = retry;
        }

        T getObj() {
            return obj;
        }

        boolean isRetry() {
            return retry;
        }

        boolean isNotRetry() {
            return !this.isRetry();
        }
    }

}