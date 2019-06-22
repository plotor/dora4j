package org.zhenchao.dora.schedule.purgatory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhenchao.dora.callback.ShutdownThread;
import org.zhenchao.dora.schedule.timer.DefaultTimer;
import org.zhenchao.dora.schedule.timer.Timer;
import org.zhenchao.dora.support.Tuple;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 延时任务推进器
 *
 * @author zhenchao.wang 2019-04-18 17:42
 * @version 1.0.0
 */
public class DelayedTaskPurgatory<T extends DelayedTask> {

    private static final Logger log = LoggerFactory.getLogger(DelayedTaskPurgatory.class);

    /** 推进器的名称 */
    private String name;
    /** 基于时间轮算法的定时器，用于从时间维度触发延时任务执行 */
    private Timer timeoutTimer;
    /** 执行任务清理的阈值，当已完成的任务数预估超过该阈值时，执行清理操作，默认为 1000 */
    private int purgeThreshold;
    /*** 是否启动后台时间轮推动器 */
    private boolean reaperEnabled;

    /** 用于创建 key 对应的 {@link Watchers} 对象 */
    private Pool<Object, Watchers> watchersForKey = new Pool<>(Watchers::new);

    private ReentrantReadWriteLock removeWatchersLock = new ReentrantReadWriteLock();

    /** 预估当前总的任务数 */
    private AtomicInteger estimatedTaskCount = new AtomicInteger(0);

    /** 后台时间轮推动器 */
    private ExpiredTaskReaper expiredTaskReaper;

    public DelayedTaskPurgatory(String name) {
        this(name, new DefaultTimer(name));
    }

    public DelayedTaskPurgatory(String name, Timer timeoutTimer) {
        this(name, timeoutTimer, 1000, true);
    }

    public DelayedTaskPurgatory(String name, Timer timeoutTimer, int purgeThreshold, boolean reaperEnabled) {
        this.name = name;
        this.timeoutTimer = timeoutTimer;
        this.purgeThreshold = purgeThreshold;
        this.reaperEnabled = reaperEnabled;
        this.expiredTaskReaper = new ExpiredTaskReaper();
        if (this.reaperEnabled) {
            log.info("[{}] start to run expired task reaper.", this.name);
            expiredTaskReaper.start();
        }
    }

    /**
     * 尝试将延时 task 添加到指定的 key(s) 对应的 Watchers 集合中，
     * 如果对应的任务已经到期，则立即执行
     *
     * @param task
     * @param watchKeys
     * @return
     */
    public boolean tryCompleteElseWatch(T task, List<Object> watchKeys) {
        if (null == watchKeys || watchKeys.size() == 0) {
            throw new IllegalArgumentException("missing watch key(s)");
        }

        // 尝试执行延时任务
        log.info("[{}] try to complete delay task [{}].", name, task.getName());
        boolean isCompleted = task.blockTryComplete();
        if (isCompleted) {
            return true;
        }

        boolean watchCreated = false;
        for (final Object key : watchKeys) {
            // 如果对应的延时任务已经执行完成，则不添加
            if (task.isCompleted()) {
                return false;
            }

            // 添加延时任务到对应 key 的 Watchers 集合中
            this.watchForTask(key, task);
            if (!watchCreated) {
                watchCreated = true;
                // 增加任务总数计数，一个任务可能会被添加到多个 key 的 Watchers 集合中，但是只计数一次
                estimatedTaskCount.incrementAndGet();
            }
            log.info("[{}] add watch task [{}] for key[{}], taskCount[{}]", name, task.getName(), key, estimatedTaskCount.get());
        }

        // 再次尝试执行延时任务
        log.info("[{}] try to complete delay task [{}] again.", name, task.getName());
        isCompleted = task.blockTryComplete();
        if (isCompleted) {
            return true;
        }

        // 将延时任务添加到 DefaultTimer 中，等待延时调度
        if (!task.isCompleted()) {
            log.info("[{}] add task [{}] to timer.", name, task.getName());
            timeoutTimer.add(task);
            // 再次检测延时任务的执行情况，如果已经完成则从 DefaultTimer 中移除
            if (task.isCompleted()) {
                task.cancel();
            }
        }

        return false;
    }

    /**
     * 尝试执行指定 key 对应的任务列表中的延时任务
     *
     * @param key
     * @return 已经执行完成的任务数
     */
    public int checkAndComplete(final Object key) {
        Watchers watchers = this.readLock(() -> watchersForKey.get(key));
        return watchers == null ? 0 : watchers.tryCompleteWatched();
    }

    /**
     * 获取所有 Watchers 集合中的任务总数，可能包括已经执行完成的
     *
     * @return
     */
    public int watched() {
        int sum = 0;
        for (final Watchers watcher : this.allWatchers()) {
            sum += watcher.countWatched();
        }
        return sum;
    }

    /**
     * 获取正在等待被执行的总任务数
     *
     * @return
     */
    public int delayed() {
        return timeoutTimer.size();
    }

    /**
     * 推动时间轮，并执行到期的任务，如果已经执行完成的任务数超过给定阈值，则执行清理操作
     *
     * @param timeoutMs 获取延时任务的超时时间
     */
    public void advanceClock(long timeoutMs) {
        timeoutTimer.advanceClock(timeoutMs);

        int delayed = this.delayed();
        if (estimatedTaskCount.get() - delayed > purgeThreshold) {
            estimatedTaskCount.getAndSet(delayed);
            log.info("[{}] start to purge watched tasks.", name);
            int purged = 0;
            for (final Watchers watcher : this.allWatchers()) {
                purged += watcher.purgeCompleted();
            }
            log.info("[{}] purged {} task from watch lists.", name, purged);
        }
    }

    /**
     * 关闭推进器
     */
    public void shutdown() {
        log.info("Shutdown [{}]", name);
        if (reaperEnabled) {
            expiredTaskReaper.shutdown();
        }
        timeoutTimer.shutdown();
    }

    /**
     * 将对应任务添加到 key 的 Watchers 集合中
     *
     * @param key
     * @param task
     */
    private void watchForTask(final Object key, final T task) {
        this.readLock((Supplier<Void>) () -> {
            // 获取 key 对应的 Watchers 对象，如果不存在则创建一个
            Watchers watcher = watchersForKey.getAndMaybePut(key);
            watcher.watch(task);
            return null;
        });
    }

    /**
     * 如果指定 key 的 Watchers 已为空，则应该移除对应的 key，防止内存泄露
     *
     * @param key
     * @param watchers
     */
    private void removeKeyIfEmpty(final Object key, final Watchers watchers) {
        this.writeLock((Supplier<Void>) () -> {
            if (watchersForKey.get(key) != watchers) {
                return null;
            }
            if (watchers != null && watchers.isEmpty()) {
                watchersForKey.remove(key);
            }
            return null;
        });
    }

    private Iterable<Watchers> allWatchers() {
        return this.readLock(() -> watchersForKey.values());
    }

    private <V> V readLock(Supplier<V> supplier) {
        try {
            removeWatchersLock.readLock().lock();
            return supplier.get();
        } finally {
            removeWatchersLock.readLock().unlock();
        }
    }

    private <V> V writeLock(Supplier<V> supplier) {
        try {
            removeWatchersLock.writeLock().lock();
            return supplier.get();
        } finally {
            removeWatchersLock.writeLock().unlock();
        }
    }

    /**
     * 封装 key 以及对应的延时任务列表
     */
    private class Watchers {

        private Object key;

        private Queue<T> tasks = new ConcurrentLinkedQueue<T>();

        public Watchers(Object key) {
            this.key = key;
        }

        public int countWatched() {
            return tasks.size();
        }

        public boolean isEmpty() {
            return tasks.isEmpty();
        }

        public void watch(T task) {
            tasks.add(task);
        }

        /**
         * 尝试执行 Watchers 中的延时任务
         *
         * @return
         */
        public int tryCompleteWatched() {
            int completed = 0;

            Iterator<T> iter = tasks.iterator();
            while (iter.hasNext()) {
                T curr = iter.next();
                if (curr.isCompleted()) {
                    // 移除已经执行完成的任务
                    iter.remove();
                }
                // 尝试执行延时任务
                else if (curr.blockTryComplete()) {
                    iter.remove();
                    completed += 1;
                }
            }

            if (tasks.isEmpty()) {
                // 如果对应的 key 的任务列表已经全部执行完成，则需要移除当前 key 对象
                DelayedTaskPurgatory.this.removeKeyIfEmpty(key, this);
            }

            return completed;
        }

        public int purgeCompleted() {
            int purged = 0;

            Iterator<T> iter = tasks.iterator();
            while (iter.hasNext()) {
                T curr = iter.next();
                if (curr.isCompleted()) {
                    iter.remove();
                    purged += 1;
                }
            }

            if (tasks.isEmpty()) {
                DelayedTaskPurgatory.this.removeKeyIfEmpty(key, this);
            }

            return purged;
        }

    }

    /**
     * 用于后台推动时间轮，执行已到期的任务
     */
    private class ExpiredTaskReaper extends ShutdownThread {

        public static final String DEFAULT_NAME = "expired-task-reaper";

        public ExpiredTaskReaper() {
            this(DEFAULT_NAME, false);
        }

        public ExpiredTaskReaper(String name, boolean interrupt) {
            super(name, interrupt);
        }

        @Override
        protected void doWork() {
            DelayedTaskPurgatory.this.advanceClock(200L);
        }
    }

    /**
     * 基于 ConcurrentHashMap 实现的 pool，提供了一些定制化的功能
     *
     * @param <K>
     * @param <V>
     */
    private class Pool<K, V> implements Iterable<Tuple<K, V>> {

        private ConcurrentMap<K, V> pool = new ConcurrentHashMap<K, V>();
        private final Object createLock = new Object();
        private Function<K, V> valueFactory;

        public Pool(Function<K, V> valueFactory) {
            this.valueFactory = valueFactory;
        }

        public Pool(Map<K, V> map) {
            for (final Map.Entry<K, V> entry : map.entrySet()) {
                pool.put(entry.getKey(), entry.getValue());
            }
        }

        public V put(K k, V v) {
            return pool.put(k, v);
        }

        public V putIfAbsent(K k, V v) {
            return pool.putIfAbsent(k, v);
        }

        /**
         * 获取 key 对应的 value，如果不存在或者 value 为 null，则使用 {@link Pool#valueFactory} 依据 key 计算对应的 createValue
         *
         * @param key key
         * @return key 对应的 value
         */
        public V getAndMaybePut(K key) {
            if (null == valueFactory) {
                throw new IllegalStateException("empty value factory in pool");
            }
            return this.getAndMaybePut(key, valueFactory.apply(key));
        }

        /**
         * 获取 key 对应的 value，如果不存在则执行 put(key, createValue) 操作
         *
         * @param key key
         * @param createValue 如果对应的 value 不存在或者为 null，则使用 createValue 更新对应的 key
         * @return
         */
        public V getAndMaybePut(K key, V createValue) {
            V current = pool.get(key);
            if (current == null) {
                synchronized (createLock) {
                    current = pool.get(key);
                    if (current == null) {
                        pool.put(key, createValue);
                        return createValue;
                    }
                    return current;
                }
            } else {
                return current;
            }
        }

        public boolean contains(K key) {
            return pool.containsKey(key);
        }

        public V get(K key) {
            return pool.get(key);
        }

        public V remove(K key) {
            return pool.remove(key);
        }

        public boolean remove(K key, V value) {
            return pool.remove(key, value);
        }

        public Set<K> keys() {
            return pool.keySet();
        }

        public Iterable<V> values() {
            return pool.values();
        }

        public void clear() {
            pool.clear();
        }

        public int size() {
            return pool.size();
        }

        @Override
        public Iterator<Tuple<K, V>> iterator() {
            return new Iterator<Tuple<K, V>>() {

                private Iterator<Map.Entry<K, V>> itr = pool.entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public Tuple<K, V> next() {
                    Map.Entry<K, V> entry = itr.next();
                    return new Tuple<>(entry.getKey(), entry.getValue());
                }

                @Override
                public void remove() {
                    itr.remove();
                }
            };
        }
    }
}
