package com.qytech.ledclock

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.qytech.ledclock.utils.NetworkManager
import kotlinx.coroutines.launch
import timber.log.Timber

class LedClockService : Service() {
    private val binder = LedClockBinder()
    private val serialPortManager by lazy {
        SerialPortManager()
    }

    private lateinit var context: Context

    inner class LedClockBinder : Binder() {
        fun getService(): LedClockService = this@LedClockService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        context = LedClockApplication.instance().applicationContext
        serialPortManager.launch {
            serialPortManager.readSerialPort()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serialPortManager.onCleared()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(LedClockUpdateReceiver.EXTRA_ACTION)) {
            LedClockUpdateReceiver.ACTION_FLAG -> {
                Timber.d("onStartCommand update flag")
                updateFlag()
            }
            LedClockUpdateReceiver.ACTION_TIME -> {
                Timber.d("onStartCommand update time")
                updateTime()
            }
            else -> {
                Timber.d("onStartCommand boot complete")
                updateFlag()
                updateTime()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateFlag() {
        val isConnectedWifi = NetworkManager.getInstance(context).isConnectedWifi()
        val isConnectedMobile = NetworkManager.getInstance(context).isConnectedMobile()
        serialPortManager.setLedClockFlag(isConnectedWifi, isConnectedMobile)
    }

    private fun updateTime() {
        serialPortManager.setLedClockTime()
    }
}