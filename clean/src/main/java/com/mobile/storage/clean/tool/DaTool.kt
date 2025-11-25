package com.mobile.storage.clean.tool

import android.util.Log
import com.mobile.storage.clean.storage.DeviceStorage
import org.json.JSONObject

object DaTool {
    fun showLog(msg: String) {
        Log.e("PDF", msg)
    }

    fun kapa(dataString: String): Boolean {
        try {
            val jsonObject = JSONObject(dataString)
            val user = jsonObject.getString("uyx")
            return user == "kapa"
        } catch (e: Exception) {
            return false
        }
    }

    fun papa(): Boolean {
        try {
            val jsonObject = JSONObject(DeviceStorage.adata)
            val user = jsonObject.getString("vf_rg")
            return user == "ppd"
        } catch (e: Exception) {
            return false
        }
    }

    fun zhiding(): Boolean {
        try {
            val jsonObject = JSONObject(DeviceStorage.adata)
            val user = jsonObject.getString("m_d_v")
            return user.isNotEmpty()
        } catch (e: Exception) {
            return false
        }
    }
    fun zhidingValue(): String {
        try {
            val jsonObject = JSONObject(DeviceStorage.adata)
            val user = jsonObject.getString("m_d_v")
            return user
        } catch (e: Exception) {
            return ""
        }
    }
}