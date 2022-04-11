package com.qytech.ledclock.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber

class NetworkManager private constructor(context: Context) {
    companion object {

        @Volatile
        private var INSTANCE: NetworkManager? = null

        fun getInstance(context: Context): NetworkManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkManager(context)
            }
    }

    private val networkCapabilities by lazy {
        val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        Timber.d("getNetworkCapabilities ${cm.hashCode()}")
        cm.getNetworkCapabilities(cm.activeNetwork)
    }

    fun isConnectedWifi(): Boolean =
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false

    fun isConnectedMobile() =
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
}
