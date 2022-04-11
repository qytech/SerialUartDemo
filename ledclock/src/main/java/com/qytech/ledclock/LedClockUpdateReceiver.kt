package com.qytech.ledclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import timber.log.Timber

class LedClockUpdateReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_ACTION = "action"
        const val ACTION_FLAG = "flag"
        const val ACTION_TIME = "time"
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("receive ${intent.action}")
        val ledClockIntent = Intent(context, LedClockService::class.java)
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                ledClockIntent.putExtra(EXTRA_ACTION, ACTION_TIME)
            }
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                Timber.d("network connect change")
                ledClockIntent.putExtra(EXTRA_ACTION, ACTION_FLAG)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Timber.d("system boot completed")
            }
        }
        context.startService(ledClockIntent)
    }
}