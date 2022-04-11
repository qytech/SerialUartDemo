package com.qytech.ledclock

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import timber.log.Timber
import kotlin.properties.Delegates


class LedClockApplication : Application() {
    companion object {
        const val PREFIX_TAG = "LedClock"
        private var INSTANCE: LedClockApplication by Delegates.notNull()

        fun instance() = INSTANCE

    }

    private val debugTree = object : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, "${PREFIX_TAG}_${tag}", message, t)
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        if (BuildConfig.DEBUG) {
            Timber.plant(debugTree)
        }
        registerNetworkListener()
    }

    @Suppress("DEPRECATION")
    private fun registerNetworkListener() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(LedClockUpdateReceiver(), intentFilter)
    }
}