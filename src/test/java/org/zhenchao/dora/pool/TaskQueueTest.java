package org.zhenchao.dora.pool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class TaskQueueTest {

    @Test
    public void testOffer1() throws Exception {
        Assertions.assertThrows(RejectedExecutionException.class, () -> {
            TaskQueue<Runnable> queue = new TaskQueue<Runnable>(1);
            queue.offer(mock(Runnable.class));
        });
    }

    @Test
    public void testOffer2() throws Exception {
        TaskQueue<Runnable> queue = new TaskQueue<Runnable>(1);
        EagerThreadPoolExecutor executor = mock(EagerThreadPoolExecutor.class);
        Mockito.when(executor.getPoolSize()).thenReturn(2);
        Mockito.when(executor.getSubmittedTaskCount()).thenReturn(1);
        queue.setExecutor(executor);
        assertThat(queue.offer(mock(Runnable.class)), is(true));
    }

    @Test
    public void testOffer3() throws Exception {
        TaskQueue<Runnable> queue = new TaskQueue<Runnable>(1);
        EagerThreadPoolExecutor executor = mock(EagerThreadPoolExecutor.class);
        Mockito.when(executor.getPoolSize()).thenReturn(2);
        Mockito.when(executor.getSubmittedTaskCount()).thenReturn(2);
        Mockito.when(executor.getMaximumPoolSize()).thenReturn(4);
        queue.setExecutor(executor);
        assertThat(queue.offer(mock(Runnable.class)), is(false));
    }

    @Test
    public void testOffer4() throws Exception {
        TaskQueue<Runnable> queue = new TaskQueue<Runnable>(1);
        EagerThreadPoolExecutor executor = mock(EagerThreadPoolExecutor.class);
        Mockito.when(executor.getPoolSize()).thenReturn(4);
        Mockito.when(executor.getSubmittedTaskCount()).thenReturn(4);
        Mockito.when(executor.getMaximumPoolSize()).thenReturn(4);
        queue.setExecutor(executor);
        assertThat(queue.offer(mock(Runnable.class)), is(true));
    }

    @Test
    public void testRetryOffer1() throws Exception {
        Assertions.assertThrows(RejectedExecutionException.class, () -> {
            TaskQueue<Runnable> queue = new TaskQueue<Runnable>(1);
            EagerThreadPoolExecutor executor = mock(EagerThreadPoolExecutor.class);
            Mockito.when(executor.isShutdown()).thenReturn(true);
            queue.setExecutor(executor);
            queue.retryOffer(mock(Runnable.class), 1000, TimeUnit.MILLISECONDS);
        });
    }

    @Test
    public void testRetryOffer2() throws Exception {
        TaskQueue<Runnable> queue = new TaskQueue<Runnable>(1);
        EagerThreadPoolExecutor executor = mock(EagerThreadPoolExecutor.class);
        Mockito.when(executor.isShutdown()).thenReturn(false);
        queue.setExecutor(executor);
        assertThat(queue.retryOffer(mock(Runnable.class), 1000, TimeUnit.MILLISECONDS), is(true));
    }

}
