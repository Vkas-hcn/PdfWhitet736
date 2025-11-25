package com.mobile.storage.clean.ref

import android.app.Application
import android.content.Context
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.mobile.storage.clean.storage.DataPing
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.tool.DaTool
import com.mobile.storage.clean.tool.PostTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

object DaoneT {
    private const val REQUIRED_MAX_RETRY = 20
    private const val OPTIONAL_MIN_RETRY = 2
    private const val OPTIONAL_MAX_RETRY = 5
    private const val MIN_DELAY_MS = 10_000L
    private const val MAX_DELAY_MS = 40_000L

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val requestingKeys = mutableSetOf<String>()


    fun initFb(app: Application, jsonObject: JSONObject) {
        try {
            val fbStr = jsonObject.optString("vmkfs").split("-")[0]
            val token = jsonObject.optString("vmkfs").split("-")[1]
            if (fbStr.isBlank()) return
            if (token.isBlank()) return
            if (FacebookSdk.isInitialized()) return
            FacebookSdk.setApplicationId(fbStr)
            FacebookSdk.setClientToken(token)
            FacebookSdk.sdkInitialize(app)
            AppEventsLogger.Companion.activateApp(app)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun postPointFun(
        context: Context,
        canRetry: Boolean,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null
    ) {
        val requestKey = "point_$name"
        val maxRetry = if (canRetry) REQUIRED_MAX_RETRY else Random.nextInt(
            OPTIONAL_MIN_RETRY,
            OPTIONAL_MAX_RETRY + 1
        )
        if (!canRetry && DeviceStorage.adata.isNotBlank() && !(DaTool.papa())) {
            return
        }
        executeWithRetry(
            requestKey = requestKey,
            maxRetry = maxRetry,
            taskName = "postPointFun[$name]",
            dataProvider = { DataPing.upPointJson(context, name, key1, keyValue1) }
        )
    }


    fun postAdJson(
        context: Context, jsonData: String
    ) {
        val requestKey = "ad_${jsonData.hashCode()}"

        executeWithRetry(
            requestKey = requestKey,
            maxRetry = REQUIRED_MAX_RETRY,
            taskName = "postAdJson",
            dataProvider = { DataPing.upAdJson(jsonData, context) }
        )
    }


    fun postInstallJson(context: Context) {
        // 检查是否已经请求成功过（insState 以 "SUCCESS:" 开头说明成功过）
        if (DeviceStorage.insState.startsWith("SUCCESS:")) {
            DaTool.showLog("postInstallJson: Already posted successfully, skipping")
            return
        }
        
        // 获取或复用安装数据
        val installData = getOrSaveInstallData(context)
        if (installData.isEmpty()) {
            DaTool.showLog("postInstallJson: Failed to get install data")
            return
        }
        
        DaTool.showLog("postInstallJson: Starting request with saved data")
        
        executeWithRetry(
            requestKey = "install",
            maxRetry = REQUIRED_MAX_RETRY,
            taskName = "postInstallJson",
            dataProvider = { installData }, // 所有重试都使用同一份保存的数据
            onSuccessCallback = {
                // 请求成功后，标记为成功（加上 SUCCESS: 前缀）
                DeviceStorage.insState = "SUCCESS:$installData"
            }
        )
    }
    

    private fun getOrSaveInstallData(context: Context): String {
        return try {
            val savedState = DeviceStorage.insState
            
            when {
                // 情况1：已经有数据，使用保存的数据（即使 App 重启）
                savedState.isNotEmpty() && !savedState.startsWith("SUCCESS:") -> {
                    DaTool.showLog("postInstallJson: Reusing saved install data (after app restart)")
                    savedState
                }
                // 情况2：第一次获取数据
                savedState.isEmpty() -> {
                    val newData = DataPing.upInstallJson(context)
                    if (newData.isNotEmpty()) {
                        DeviceStorage.insState = newData
                        DaTool.showLog("postInstallJson: First time getting and saving install data")
                    }
                    newData
                }
                // 情况3：已成功的情况（不应该走到这里）
                else -> {
                    DaTool.showLog("postInstallJson: Unexpected state - already successful")
                    ""
                }
            }
        } catch (e: Exception) {
            DaTool.showLog("postInstallJson: Error getting install data - ${e.message}")
            e.printStackTrace()
            // 发生异常时，如果有保存的数据则使用，否则返回空
            val savedState = DeviceStorage.insState
            if (savedState.isNotEmpty() && !savedState.startsWith("SUCCESS:")) {
                savedState
            } else {
                ""
            }
        }
    }


    private fun executeWithRetry(
        requestKey: String,
        maxRetry: Int,
        taskName: String,
        dataProvider: () -> String,
        onSuccessCallback: (() -> Unit)? = null,
        currentAttempt: Int = 0
    ) {
        try {
            // 防止重复请求
            if (requestingKeys.contains(requestKey)) {
                return
            }

            // 标记为请求中
            requestingKeys.add(requestKey)

            val jsonData = dataProvider()
            DaTool.showLog("post-${taskName}-json: ${jsonData}")
            PostTool.postPutData(jsonData, object : PostTool.CallbackMy {
                override fun onSuccess(result: String) {
                    requestingKeys.remove(requestKey)
                    onSuccessCallback?.invoke()
                    DaTool.showLog("post-${taskName}-Success: ${result}")
                }

                override fun onFailure(error: String) {

                    DaTool.showLog("post-${taskName}-error: ${error}")
                    if (currentAttempt < maxRetry) {
                        // 计算随机延迟时间
                        val delayMs = Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1)

                        // 使用协程延迟后重试
                        coroutineScope.launch {
                            delay(delayMs)
                            requestingKeys.remove(requestKey)
                            executeWithRetry(
                                requestKey,
                                maxRetry,
                                taskName,
                                dataProvider,
                                onSuccessCallback,
                                currentAttempt + 1
                            )
                        }
                    } else {
                        requestingKeys.remove(requestKey)
                    }
                }
            })
        } catch (e: Exception) {
            DaTool.showLog("post-${taskName}-error: ${e.message}")

            requestingKeys.remove(requestKey)
        }
    }

    fun ConfigG(context: Context,typeUser: Boolean, codeInt: String?) {
        val isuserData: String? = if (codeInt == null) {
            null
        } else if (codeInt != "200") {
            codeInt
        } else if (typeUser) {
            "a"
        } else {
            "b"
        }
        postPointFun(context,true, "config_G", "getstring", isuserData)
    }
    fun cfFail(context: Context){
        val currentSavedData = JSONObject(DeviceStorage.adata)
        if(currentSavedData.optString("uyx").isEmpty()){
            DaoneT.postPointFun(context,true, "cf_fail")
        }
    }
}