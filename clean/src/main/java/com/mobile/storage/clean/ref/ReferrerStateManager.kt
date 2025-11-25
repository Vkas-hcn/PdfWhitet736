package com.mobile.storage.clean.ref

import com.mobile.storage.clean.storage.DeviceStorage

/**
 * Referrer 状态管理器
 * 职责：管理 Referrer 获取状态和缓存检查
 */
object ReferrerStateManager {
    
    // 是否正在获取中
    private var isGettingReferrer = false
    
    /**
     * 检查是否已有 Referrer 缓存
     */
    fun hasReferrerCache(): Boolean {
        return DeviceStorage.rid.isNotEmpty()
    }
    
    /**
     * 检查是否正在获取中
     */
    fun isInProgress(): Boolean {
        return isGettingReferrer
    }
    
    /**
     * 标记开始获取
     */
    fun markStartGetting() {
        isGettingReferrer = true
    }
    
    /**
     * 标记结束获取
     */
    fun markEndGetting() {
        isGettingReferrer = false
    }
    
    /**
     * 保存 Referrer 数据到缓存
     */
    fun saveReferrerData(referrer: String, clickTime: Long, serverTime: Long) {
        DeviceStorage.rid = referrer
        DeviceStorage.rcts = clickTime.toString()
        DeviceStorage.rctss = serverTime.toString()
    }
}
