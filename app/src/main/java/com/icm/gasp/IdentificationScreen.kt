package com.icm.gasp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.icm.gasp.utils.PreferencesManager
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress

class IdentificationScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identification_screen)

        // Obtén el TextView
        val ipListTextView: TextView = findViewById(R.id.ip_list_text_view)
        val ipAndMacTextView: TextView = findViewById(R.id.ip_and_mac)

        // Obtén la lista de IPs desde SharedPreferences
        val ipList = PreferencesManager.getIpList(this)

        // Muestra la lista de IPs en el TextView
        ipListTextView.text = "IPs Conectadas:\n${ipList.joinToString("\n")}"

        // Muestra la lista de IPs y MACs en el otro TextView
        val ipAndMacList = ipList.map { ip -> "$ip - ${getMacAddress(ip)}" }
        ipAndMacTextView.text = "IPs y MACs Conectadas:\n${ipAndMacList.joinToString("\n")}"
    }

    private fun getMacAddress(ip: String): String {
        return try {
            val bufferedReader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val parts = line!!.split("\\s+".toRegex())
                if (parts.size >= 4 && parts[0] == ip) {
                    bufferedReader.close()
                    return parts[3]
                }
            }
            bufferedReader.close()
            "MAC no disponible"
        } catch (e: Exception) {
            e.printStackTrace()
            "MAC no disponible"
        }
    }
}
