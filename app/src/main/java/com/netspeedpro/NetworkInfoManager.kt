package com.netspeedpro

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class NetworkInfo(
    val isConnected: Boolean,
    val networkType: String,
    val carrier: String,
    val generation: String,
    val ipAddress: String,
    val isInternetAvailable: Boolean
)

object NetworkInfoManager {

    suspend fun getNetworkInfo(context: Context): NetworkInfo = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(net)

        val isConnected = caps != null
        val isInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val transportType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> "VPN"
            else -> if (isConnected) "Unknown" else "No Connection"
        }

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val carrier = try {
            val name = tm.networkOperatorName
            if (name.isNullOrBlank()) "Unknown" else name
        } catch (_: Exception) { "Unknown" }

        val generation = try {
            when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G / LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "N/A"
            }
        } catch (_: Exception) { "N/A" }

        val ip = try {
            InetAddress.getLocalHost().hostAddress ?: "—"
        } catch (_: Exception) {
            try {
                val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
                var found = "—"
                for (iface in interfaces) {
                    for (addr in iface.inetAddresses) {
                        if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                            found = addr.hostAddress ?: "—"
                            break
                        }
                    }
                    if (found != "—") break
                }
                found
            } catch (_: Exception) { "—" }
        }

        NetworkInfo(
            isConnected = isConnected,
            networkType = transportType,
            carrier = carrier,
            generation = generation,
            ipAddress = ip,
            isInternetAvailable = isInternet
        )
    }
}
