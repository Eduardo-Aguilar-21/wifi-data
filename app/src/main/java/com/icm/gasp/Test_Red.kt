package com.icm.gasp

import NetworkChangeReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper

class Test_Red : AppCompatActivity() {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_red)

        val isConnectedTextView: TextView = findViewById(R.id.isConected)
        val ipLocal: TextView = findViewById(R.id.ipLocal)
        val ipPublicTextView: TextView = findViewById(R.id.ipPublica)
        val ipRouterTextView: TextView = findViewById(R.id.ipRouter)
        val devicesIpTextView: TextView = findViewById(R.id.devicesIp)

        handler = Handler(Looper.getMainLooper())
        networkChangeReceiver = NetworkChangeReceiver(isConnectedTextView, ipLocal, ipPublicTextView, ipRouterTextView, devicesIpTextView, handler)

        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)

        networkChangeReceiver.onReceive(this, Intent())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }
}
