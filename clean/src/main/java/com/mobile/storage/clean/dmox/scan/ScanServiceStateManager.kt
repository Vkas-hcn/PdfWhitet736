package com.mobile.storage.clean.dmox.scan

import com.mobile.storage.clean.storage.DeviceStorage

/**
 * 扫描服务状态管理器
 * 职责：负责管理服务的运行状态
 */
object ScanServiceStateManager {
    
    /**
     * 标记服务已启动
     */
    fun markServiceStarted() {
        DeviceStorage.serviceSate = true
    }
    
    /**
     * 标记服务已停止
     */
    fun markServiceStopped() {
        DeviceStorage.serviceSate = false
    }
    
    /**
     * 检查服务是否正在运行
     */
    fun isServiceRunning(): Boolean {
        return DeviceStorage.serviceSate
    }
}
