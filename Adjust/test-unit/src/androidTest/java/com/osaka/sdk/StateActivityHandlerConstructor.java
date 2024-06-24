package com.osaka.sdk;

import com.osaka.sdk.ActivityHandler;
import com.osaka.sdk.AdjustConfig;
import com.osaka.sdk.IRunActivityHandler;
import com.osaka.sdk.LogLevel;

/**
 * Created by pfms on 09/08/2016.
 */
public class StateActivityHandlerConstructor {
    AdjustConfig config;
    boolean startEnabled = true;
    boolean isToUpdatePackages = false;

    StateActivityHandlerConstructor(AdjustConfig config) {
        this.config = config;
    }
}
