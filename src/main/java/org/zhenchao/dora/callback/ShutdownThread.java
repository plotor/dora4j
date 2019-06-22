package org.zhenchao.dora.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhenchao.wang 2019-04-18 18:07
 * @version 1.0.0
 */
public abstract class ShutdownThread extends Thread {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private String name;
    private boolean interrupt;

    private AtomicBoolean running = new AtomicBoolean(true);
    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    public ShutdownThread(String name, boolean interrupt) {
        super(name);
        this.name = name;
        this.interrupt = interrupt;
        this.setDaemon(false);
    }

    public void shutdown() {
        this.initiateShutdown();
        this.awaitShutdown();
    }

    public boolean initiateShutdown() {
        if (running.compareAndSet(true, false)) {
            log.info("Shutting down: {}", name);
            running.set(false);
            if (interrupt) {
                this.interrupt();
            }
            return true;
        }
        return false;
    }

    public void awaitShutdown() {
        try {
            shutdownLatch.await();
            log.info("Shutdown completed: {}", name);
        } catch (InterruptedException e) {
            log.info("Shutdown error: {}", name, e);
        }
    }

    /**
     * 执行具体的业务逻辑
     */
    protected abstract void doWork();

    @Override
    public void run() {
        log.info("Starting job: {}", name);
        try {
            while (running.get()) {
                this.doWork();
            }
        } catch (Throwable e) {
            if (running.get()) {
                log.error("Error when running, name: {}", name, e);
            }
        }
        shutdownLatch.countDown();
        log.info("Stopped job: {}", name);
    }

}
