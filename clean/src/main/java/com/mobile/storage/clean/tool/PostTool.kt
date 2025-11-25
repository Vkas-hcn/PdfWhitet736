package com.mobile.storage.clean.tool


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import android.util.Base64
import android.util.Log
import com.mobile.storage.clean.ref.DaoneT
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.tool.DataUserTool.adminUrl
import com.mobile.storage.clean.tool.DataUserTool.upUrl
import com.mobile.storage.clean.tool.LifTool.showAppVersion
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.apply
import kotlin.collections.joinToString
import kotlin.io.use
import kotlin.text.mapIndexed
import kotlin.text.toByteArray
import kotlin.text.toCharArray

object PostTool {

    interface CallbackMy {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }




    @SuppressLint("HardwareIds")
    fun adminData(context: Context): String {
        return JSONObject().apply {
            put("IfjXn", "com.pdf.viewer.edit")
            put("ialfcODK", DeviceStorage.aid)
            put("MouxM", DeviceStorage.rid)
//            put("MouxM", "555")
            put("UqWFUvVOQ", showAppVersion(context))
            //referrerClickTimestampSeconds
            put("NXCfINQ",DeviceStorage.rcts)
            //referrerClickTimestampServerSeconds
            put("naUVxMKh",DeviceStorage.rctss)
            //installerPackageName
            put("dVJ",getISData(context))
        }.toString()
    }

    fun getISData(context: Context):String{
        val installerPackageName: String? = context.packageManager
            .getInstallerPackageName(context.packageName)
        return  installerPackageName?:""
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun postAdminData(context: Context,callback: CallbackMy) {
        DaTool.showLog("postAdminData=${adminData(context)}")
        val jsonBodyString = JSONObject(adminData(context)).toString()
        val dt = System.currentTimeMillis().toString()
        val xorEncryptedString = jxData(jsonBodyString, dt)
        val base64EncodedString = Base64.encodeToString(
            xorEncryptedString.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP
        )

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = base64EncodedString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(String.adminUrl)
            .post(requestBody)
            .addHeader("dt", dt)
            .build()
        DaoneT.postPointFun(context,false, "config_R")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Request failed: ${e.message}")
                DaoneT.postPointFun(context,true, "config_G", "getstring","timeout")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    callback.onFailure("Unexpected code $response")
                    DaoneT.ConfigG(context,false,response.code.toString())
                    return
                }
                try {
                    val timestampResponse = response.header("dt")
                        ?: throw kotlin.IllegalArgumentException("Timestamp missing in headers")

                    val decodedBytes = Base64.decode(response.body?.string() ?: "", Base64.DEFAULT)
                    val decodedString = String(decodedBytes, Charsets.UTF_8)
                    val finalData = jxData(decodedString, timestampResponse)
                    val jsonResponse = JSONObject(finalData)
                    val jsonData = JSONObject(parseAdminRefData(jsonResponse.toString()))
                    isCanSave(jsonData)
                    DaoneT.initFb(context as Application,jsonData)
                    val data = jsonData.toString()
                    Log.e("TAG", "onResponse: ${data}")
                    Log.e("TAG", "onResponse:NcZong.getTypeState(jsonData)=${DaTool.kapa(jsonData.toString())} ")
                    DaoneT.ConfigG(context,DaTool.kapa(jsonData.toString()),"200")
                    DaoneT.cfFail(context)
                    callback.onSuccess(jsonData.toString())
                } catch (e: Exception) {
                    callback.onFailure("Decryption failed: ${e.message}")
                }
            }
        })

    }
    /**
     * 判断是否可以保存新数据
     * 规则：如果当前数据是有效状态(kapa=true)，而新数据是无效状态(kapa=false)，则拒绝保存
     */
    fun isCanSave(jsonData: JSONObject) {
        val currentSavedData = DeviceStorage.adata
        val newDataString = jsonData.toString()
        
        // 如果当前没有保存的数据，直接保存新数据
        if (currentSavedData.isEmpty()) {
            DeviceStorage.adata = newDataString
            return
        }
        
        // 检查是否需要拒绝保存
        val shouldRejectSave = try {
            val isCurrentDataValid = DaTool.kapa(currentSavedData)
            val isNewDataValid = DaTool.kapa(newDataString)
            // 当前数据有效 且 新数据无效 -> 拒绝保存（保护有效数据不被覆盖）
            isCurrentDataValid && !isNewDataValid
        } catch (e: Exception) {
            // 如果解析失败，不拒绝保存（允许新数据覆盖无效的旧数据）
            DaTool.showLog("isCanSave check failed: ${e.message}")
            false
        }
        
        // 如果不需要拒绝，则保存新数据
        if (!shouldRejectSave) {
            DeviceStorage.adata = newDataString
        }
    }


    private fun jxData(text: String, dt: String): String {
        val cycleKey = dt.toCharArray()
        val keyLength = cycleKey.size
        return text.mapIndexed { index, char ->
            char.toInt().xor(cycleKey[index % keyLength].toInt()).toChar()
        }.joinToString("")
    }

    private fun parseAdminRefData(jsonString: String): String {
        try {
            val confString = JSONObject(jsonString).getJSONObject("VTnYOi").getString("conf")
            return confString
        } catch (e: Exception) {
            return ""
        }
    }

    fun postPutData(body: Any, callbackData: CallbackMy) {
        val jsonBodyString = JSONObject(body.toString()).toString()
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBodyString
        )

        val request = Request.Builder()
            .url(String.upUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callbackData.onFailure(e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callbackData.onFailure("Unexpected code $response")
                    } else {
                        val responseData = response.body?.string() ?: ""
                        callbackData.onSuccess(responseData)
                    }
                }
            }
        })
    }

}
