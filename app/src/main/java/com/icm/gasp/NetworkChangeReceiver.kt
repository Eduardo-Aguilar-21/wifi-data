// NetworkChangeReceiver.kt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NetworkChangeReceiver(
    private val connectionTextView: TextView,
    private val ipTextView: TextView,
    private val publicIpTextView: TextView,
    private val routerIpTextView: TextView,
    private val devicesIpTextView: TextView,
    private val handler: Handler
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
        routerIpTextView.text = "IP Router: ${getRouterIpAddress(context)}"

        if (isWifi) {
            FetchDevicesIpTask(devicesIpTextView, handler, context).execute()
        } else {
            devicesIpTextView.text = "Escaneo de dispositivos disponible solo en Wi-Fi"
        }
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

    private fun getRouterIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo = wifiManager.dhcpInfo

        return if (dhcpInfo.gateway != 0) {
            val ipAddress = dhcpInfo.gateway
            String.format(
                "%d.%d.%d.%d",
                (ipAddress and 0xFF).toInt(),
                (ipAddress shr 8 and 0xFF).toInt(),
                (ipAddress shr 16 and 0xFF).toInt(),
                (ipAddress shr 24 and 0xFF).toInt()
            )
        } else {
            "IP no disponible"
        }
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

    private class FetchDevicesIpTask(
        private val textView: TextView,
        private val handler: Handler,
        private val context: Context
    ) : AsyncTask<Void, String, String>() {

        override fun doInBackground(vararg params: Void?): String {
            val sb = StringBuilder()
            val localIp = getLocalIpAddress()
            val baseIp = localIp.substringBeforeLast(".") + "."

            val ranges = listOf(100..150, 230..238)

            for (range in ranges) {
                for (i in range) {
                    val ip = baseIp + i
                    val process = Runtime.getRuntime().exec("ping -c 1 $ip")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val result = reader.readText()
                    if (result.contains("1 packets transmitted, 1 received")) {
                        sb.append(ip).append("\n")
                        publishProgress(ip)
                    }
                }
            }
            return sb.toString()
        }

        override fun onProgressUpdate(vararg values: String) {
            val ip = values[0]
            handler.post {
                textView.append("$ip\n")
            }
        }

        override fun onPostExecute(result: String) {
            handler.post {
                textView.text = "Dispositivos conectados:\n$result"
            }
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
    }
}
