package com.osaka.sdk.scheduler;

/**
 * Created by nonelse on 12.09.17.
 */

public interface ThreadExecutor {
    void submit(Runnable task);
    void teardown();
}
