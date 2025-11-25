package com.mobile.storage.clean.ref

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.mobile.storage.clean.tool.DaTool

/**
 * Install Referrer 数据获取协调器
 * 职责：协调各个管理器，管理 Referrer 获取流程
 * 
 * 架构说明：
 * - ReferrerStateManager: 负责状态管理和缓存检查
 * - ReferrerClientManager: 负责 InstallReferrerClient 的连接和数据获取
 * - ReferrerRetryManager: 负责超时和重试逻辑
 * - ReferrerDataProcessor: 负责数据处理和后续流程触发
 */
object DataOne {
    
    // 客户端管理器
    private var clientManager: ReferrerClientManager? = null
    
    // 重试管理器
    private var retryManager: ReferrerRetryManager? = null
    
    /**
     * 获取 Install Referrer 信息
     * @param context 上下文
     */
    fun fetchInstallReferrer(context: Context) {
        // 1. 检查缓存：如果已有 referrer，直接处理后续流程
        if (ReferrerStateManager.hasReferrerCache()) {
            DaTool.showLog("DataOne: Referrer already cached, processing next steps")
            ReferrerDataProcessor.processAndTriggerNext(context)
            return
        }
        
        // 2. 检查进度：如果正在获取中，不重复执行
        if (ReferrerStateManager.isInProgress()) {
            DaTool.showLog("DataOne: Referrer fetch already in progress")
            return
        }
        
        // 3. 标记开始获取
        ReferrerStateManager.markStartGetting()
        
        // 4. 初始化管理器
        initializeManagers()
        
        // 5. 设置超时和重试
        setupTimeoutAndRetry(context)
        
        // 6. 连接 Referrer 服务
        connectToReferrerService(context)
    }
    
    /**
     * 初始化管理器
     */
    private fun initializeManagers() {
        clientManager = ReferrerClientManager()
        retryManager = ReferrerRetryManager()
    }
    
    /**
     * 设置超时和重试机制
     */
    private fun setupTimeoutAndRetry(context: Context) {
        retryManager?.setupTimeout(context) {
            // 超时回调：清理并重试
            cleanupResources()
            retryManager?.scheduleRetry(context) {
                fetchInstallReferrer(context)
            }
        }
    }
    
    /**
     * 连接到 Referrer 服务
     */
    private fun connectToReferrerService(context: Context) {
        try {
            clientManager?.connectToReferrerService(
                context,
                createReferrerStateListener(context)
            )
        } catch (e: Exception) {
            DaTool.showLog("DataOne: Connection failed - ${e.message}")
            e.printStackTrace()
            handleConnectionFailure(context)
        }
    }
    
    /**
     * 创建 Referrer 状态监听器
     */
    private fun createReferrerStateListener(context: Context): InstallReferrerStateListener {
        return object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        handleSuccessResponse(context)
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        handleFailure(context, "Feature not supported")
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        handleFailure(context, "Service unavailable")
                    }
                    else -> {
                        handleFailure(context, "Unknown response code: $responseCode")
                    }
                }
            }
            
            override fun onInstallReferrerServiceDisconnected() {
                handleFailure(context, "Service disconnected")
            }
        }
    }
    
    /**
     * 处理成功响应
     */
    private fun handleSuccessResponse(context: Context) {
        try {
            val details = clientManager?.getReferrerDetails()
            if (details != null) {
                // 保存 Referrer 数据
                ReferrerStateManager.saveReferrerData(
                    details.referrer,
                    details.clickTime,
                    details.serverTime
                )
                
                DaTool.showLog("DataOne: Referrer obtained successfully")
                
                // 处理后续流程
                ReferrerDataProcessor.processAndTriggerNext(context)
                
                // 取消超时并清理资源
                retryManager?.cancelTimeout()
                cleanupResources()
            } else {
                DaTool.showLog("DataOne: Referrer details is null")
                handleFailure(context, "Referrer details is null")
            }
        } catch (e: Exception) {
            DaTool.showLog("DataOne: Error processing referrer - ${e.message}")
            e.printStackTrace()
            handleFailure(context, "Error: ${e.message}")
        }
    }
    
    /**
     * 处理失败情况
     */
    private fun handleFailure(context: Context, reason: String) {
        DaTool.showLog("DataOne: Failed - $reason, will retry")
        
        // 取消超时
        retryManager?.cancelTimeout()
        
        // 清理资源
        cleanupResources()
        
        // 延迟重试
        retryManager?.scheduleRetry(context) {
            fetchInstallReferrer(context)
        }
    }
    
    /**
     * 处理连接失败
     */
    private fun handleConnectionFailure(context: Context) {
        cleanupResources()
        retryManager?.scheduleRetry(context) {
            fetchInstallReferrer(context)
        }
    }
    
    /**
     * 清理资源
     */
    private fun cleanupResources() {
        ReferrerStateManager.markEndGetting()
        clientManager?.disconnectClient()
    }
}