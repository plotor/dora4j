package org.zhenchao.storm.thread;

/**
 * @author zhenchao.wang 2018-06-14 18:08
 * @version 1.0.0
 */
public interface SmartThread {

    void start();

    void join() throws InterruptedException;

    void interrupt();

    Boolean isSleeping();

    void cleanup();

}