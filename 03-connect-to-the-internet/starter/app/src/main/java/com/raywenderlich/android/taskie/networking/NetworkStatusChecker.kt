package com.raywenderlich.android.taskie.networking

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkStatusChecker(private val connectivityManager: ConnectivityManager) {

    inline fun performIfConnectedToInternet(action: () -> Unit) {
        if (hasInternetConnection()) {
            action()
        }
    }

    fun hasInternetConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return with(capabilities) {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    }
}