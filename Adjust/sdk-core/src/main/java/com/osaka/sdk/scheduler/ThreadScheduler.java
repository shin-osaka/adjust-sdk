package com.osaka.sdk.scheduler;

public interface ThreadScheduler extends ThreadExecutor {
    void schedule(Runnable task, long millisecondsDelay);
}
