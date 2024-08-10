package com.icm.gasp.utils

import android.content.Context

object PreferencesManager {
    private const val PREFS_NAME = "AppPrefs"
    private const val KEY_IP_LIST = "ip_list"

    fun saveIpList(context: Context, ipList: List<String>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_IP_LIST, ipList.joinToString(","))
        editor.apply()
    }

    fun getIpList(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ipString = sharedPreferences.getString(KEY_IP_LIST, "")
        return ipString?.split(",") ?: emptyList()
    }
}
