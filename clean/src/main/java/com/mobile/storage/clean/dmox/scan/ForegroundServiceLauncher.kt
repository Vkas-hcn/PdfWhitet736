package com.mobile.storage.clean.dmox.scan

import android.app.Notification
import android.app.Service
import com.mobile.storage.clean.tool.DaTool

/**
 * 前台服务启动器
 * 职责：负责安全启动前台服务
 */
class ForegroundServiceLauncher(private val service: Service) {
    
    /**
     * 启动前台服务
     * @param notificationId 通知 ID
     * @param notification 通知对象
     */
    fun startForeground(notificationId: Int, notification: Notification?) {
        runCatching {
            service.startForeground(notificationId, notification)
            DaTool.showLog("ForegroundServiceLauncher: Service started in foreground")
        }.onFailure { error ->
            DaTool.showLog("ForegroundServiceLauncher: Failed to start foreground - ${error.message}")
        }
    }
}
