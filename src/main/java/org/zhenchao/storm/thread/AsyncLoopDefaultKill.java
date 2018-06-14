package org.zhenchao.storm.thread;

import org.zhenchao.util.ThreadUtils;

/**
 * Killer callback, 当进程被 kill 时默认的回调函数
 *
 * @author zhenchao.wang 2018-06-14 18:11
 * @version 1.0.0
 */
public class AsyncLoopDefaultKill extends RunnableCallback {

    @Override
    public <T> Object execute(T... args) {
        Exception e = (Exception) args[0];
        ThreadUtils.haltProcess(1);
        return e;
    }

    @Override
    public void run() {
        ThreadUtils.haltProcess(1);
    }
}

