package com.mobile.storage.clean.storage

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.mobile.storage.clean.tool.DaTool
import com.mobile.storage.clean.tool.LifTool
import org.json.JSONObject
import java.util.UUID
import kotlin.apply

object DataPing {
    private fun topJsonData(context: Context): JSONObject {
        val languish = JSONObject().apply {
            //bundle_id
            put("lena", context.packageName)

            //app_version
            put("been", LifTool.showAppVersion(context))
            //client_ts
            put("austral", System.currentTimeMillis())
            //os_version
            put("zag", Build.VERSION.RELEASE)
        }

        val force = JSONObject().apply {
            //os
            put("vermouth", "adjacent")
            //distinct_id
            put("excess", DeviceStorage.aid)
            //manufacturer
            put("procter", Build.MANUFACTURER)
            //device_model-最新需要传真实值
            put("mannitol", Build.BRAND)

            //operator 传假值字符串
            put("jalopy", "sdcw")
        }

        val sloe = JSONObject().apply {
            //log_id
            put("dubious", UUID.randomUUID().toString())
            //system_language//假值
            put("rubric", "casc_aex")
            //android_id
            put("befell", DeviceStorage.aid)

            //gaid
            put("confocal", "")
        }
        return JSONObject().apply {
            put("languish", languish)
            put("force", force)
            put("sloe", sloe)
            if (DaTool.zhiding()) {
                put("usercode^breathe", DaTool.zhidingValue())
            }
        }
    }

    fun upInstallJson(context: Context): String {
        val gainful = JSONObject().apply {
            //build
            put("hispanic", "build/${Build.ID}")

            //referrer_url
            put("vee", DeviceStorage.rid)

            //user_agent
            put("inhumane", "")

            //lat
            put("gauche", "sneeze")

            //referrer_click_timestamp_seconds
            put("dip", 0)

            //install_begin_timestamp_seconds
            put("elude", 0)

            //referrer_click_timestamp_server_seconds
            put("babel", 0)

            //install_begin_timestamp_server_seconds
            put("dobson", 0)

            //install_first_seconds
            put("grover", getFirstInstallTime(context))

            //last_update_seconds
            put("influent", 0)
        }
        return topJsonData(context).apply {
            put("gainful", gainful)
        }.toString()
    }

    fun upAdJson(adJson: String, context: Context): String {
        return topJsonData(context).apply {
            put("peugeot", JSONObject(adJson))
        }.toString()
    }


    fun upPointJson(
        context: Context,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null,
        key2: String? = null,
        keyValue2: Any? = null,
    ): String {
        return topJsonData(context).apply {
            put("hoagie", name)
            if (key1 != null) {
                put("harry~$key1", keyValue1)
            }
            if (key2 != null) {
                put("harry~$key2", keyValue2)
            }
        }.toString()
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }


}