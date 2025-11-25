package com.mobile.storage.clean.ref

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.mobile.storage.clean.tool.DaTool

/**
 * Referrer 客户端管理器
 * 职责：管理 InstallReferrerClient 的创建、连接和清理
 */
class ReferrerClientManager {
    
    private var referrerClient: InstallReferrerClient? = null
    
    /**
     * 连接 Referrer 服务
     * @param context 上下文
     * @param listener 状态监听器
     */
    fun connectToReferrerService(
        context: Context,
        listener: InstallReferrerStateListener
    ) {
        try {
            // 清理之前的连接
            disconnectClient()
            
            // 创建新的客户端
            referrerClient = InstallReferrerClient.newBuilder(context).build()
            
            // 开始连接
            referrerClient?.startConnection(listener)
            
            DaTool.showLog("ReferrerClientManager: Connection started")
            
        } catch (e: Exception) {
            DaTool.showLog("ReferrerClientManager: Failed to connect - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * 获取 Referrer 信息
     */
    fun getReferrerDetails(): ReferrerDetails? {
        return try {
            val response = referrerClient?.installReferrer
            if (response != null) {
                ReferrerDetails(
                    referrer = response.installReferrer,
                    clickTime = response.referrerClickTimestampSeconds,
                    serverTime = response.referrerClickTimestampServerSeconds
                )
            } else {
                null
            }
        } catch (e: Exception) {
            DaTool.showLog("ReferrerClientManager: Failed to get details - ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 断开客户端连接
     */
    fun disconnectClient() {
        try {
            referrerClient?.endConnection()
            DaTool.showLog("ReferrerClientManager: Client disconnected")
        } catch (e: Exception) {
            // 忽略清理时的异常
        }
        referrerClient = null
    }
    
    /**
     * Referrer 详情数据类
     */
    data class ReferrerDetails(
        val referrer: String,
        val clickTime: Long,
        val serverTime: Long
    )
}
