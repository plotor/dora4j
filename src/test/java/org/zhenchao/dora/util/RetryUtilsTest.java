package org.zhenchao.dora.util;

import org.junit.Assert;
import org.junit.Test;
import org.zhenchao.dora.support.IFunction;
import org.zhenchao.dora.support.ISupplier;

/**
 * @author zhenchao.wang 2018-07-19 11:12
 * @version 1.0.0
 */
public class RetryUtilsTest {

    @Test
    public void notRetry() throws Exception {
        boolean result = RetryUtils.active(new IFunction<Integer, RetryUtils.ResultWrapper<Boolean>>() {
            @Override
            public RetryUtils.ResultWrapper<Boolean> apply(Integer integer) {
                return RetryUtils.success(true);
            }
        });
        Assert.assertTrue(result);

        result = RetryUtils.active(new ISupplier<RetryUtils.ResultWrapper<Boolean>>() {
            @Override
            public RetryUtils.ResultWrapper<Boolean> get() {
                return RetryUtils.success(true);
            }
        });
        Assert.assertTrue(result);
    }

    @Test
    public void retryWithFunction() throws Exception {
        RetryUtils.active(1, new IFunction<Integer, RetryUtils.ResultWrapper<Object>>() {
            @Override
            public RetryUtils.ResultWrapper<Object> apply(Integer integer) {
                return RetryUtils.retry();
            }
        });

        System.out.println("1. ----------");

        RetryUtils.active(1, new IFunction<Integer, RetryUtils.ResultWrapper<Object>>() {
            @Override
            public RetryUtils.ResultWrapper<Object> apply(Integer integer) {
                return RetryUtils.retry();
            }
        }, 10);

        System.out.println("2. ----------");

        RetryUtils.active(1, new IFunction<Integer, RetryUtils.ResultWrapper<Object>>() {
            @Override
            public RetryUtils.ResultWrapper<Object> apply(Integer integer) {
                return RetryUtils.retry();
            }
        }, 3L);

        System.out.println("3. ----------");

        RetryUtils.active(1, new IFunction<Integer, RetryUtils.ResultWrapper<Boolean>>() {
            @Override
            public RetryUtils.ResultWrapper<Boolean> apply(Integer integer) {
                return new RetryUtils.ResultWrapper<Boolean>(false, false);
            }
        });
    }

    @Test
    public void retryWithSupplier() throws Exception {
        RetryUtils.active(new ISupplier<RetryUtils.ResultWrapper<Object>>() {
            @Override
            public RetryUtils.ResultWrapper<Object> get() {
                return RetryUtils.retry();
            }
        });

        System.out.println("1. ----------");

        RetryUtils.active(new ISupplier<RetryUtils.ResultWrapper<Object>>() {
            @Override
            public RetryUtils.ResultWrapper<Object> get() {
                return RetryUtils.retry();
            }
        }, 10);

        System.out.println("2. ----------");

        RetryUtils.active(new ISupplier<RetryUtils.ResultWrapper<Object>>() {
            @Override
            public RetryUtils.ResultWrapper<Object> get() {
                return RetryUtils.retry();
            }
        }, 3L);

        System.out.println("3. ----------");

        RetryUtils.active(new ISupplier<RetryUtils.ResultWrapper<Boolean>>() {
            @Override
            public RetryUtils.ResultWrapper<Boolean> get() {
                return new RetryUtils.ResultWrapper<Boolean>(false, false);
            }
        });
    }

    @Test
    public void retryWithException() throws Exception {
        Throwable e = RetryUtils.active(new ISupplier<RetryUtils.ResultWrapper<Throwable>>() {
            @Override
            public RetryUtils.ResultWrapper<Throwable> get() {
                try {
                    throw new IllegalStateException();
                } catch (Throwable e) {
                    return RetryUtils.retry(e);
                }
            }
        });
        if (null != e) {
            e.printStackTrace();
        }
    }
}