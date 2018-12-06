package com.qytech.serialuartdemo;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Jax on 2018/10/27.
 * Description :
 * Version : V1.0.0
 */
public class GlobalApplication extends Application {
    private static final String TAG = "SerialPort";

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.tag(TAG);
        }
    }
}
