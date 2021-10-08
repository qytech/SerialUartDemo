package com.qytech.serialportdemo

import android.app.Application
import timber.log.Timber

class SerialPortApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}