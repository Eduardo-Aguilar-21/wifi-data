package com.icm.gasp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.widget.TextView
import java.net.NetworkInterface
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NetworkChangeReceiver(
    private val connectionTextView: TextView,
    private val ipTextView: TextView,
    private val publicIpTextView: TextView
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        val isConnected = networkCapabilities != null
        val isWifi = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        connectionTextView.text = when {
            isWifi -> "Conectado a Wi-Fi"
            isCellular -> "Conectado a Datos Móviles"
            else -> "No Conectado a una red"
        }

        ipTextView.text = if (isCellular) "IP Móvil: ${getLocalIpAddress()}" else "IP Local: ${getLocalIpAddress()}"
        FetchPublicIpTask(publicIpTextView).execute()
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                val addresses = networkInterface.inetAddresses
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address.isSiteLocalAddress) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "IP no disponible"
    }

    private class FetchPublicIpTask(private val textView: TextView) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            return try {
                val url = URL("https://api.ipify.org?format=text")
                val connection = url.openConnection() as HttpsURLConnection
                connection.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                e.printStackTrace()
                "IP no disponible"
            }
        }

        override fun onPostExecute(result: String) {
            textView.text = "IP Pública: $result"
        }
    }
}
