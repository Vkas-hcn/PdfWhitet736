package com.mobile.storage.clean.dmox.scan

import android.app.Service
import android.content.Intent
import android.os.IBinder


class ScanService : Service() {
    
    // 通知管理器
    private lateinit var notificationManager: ScanNotificationManager
    
    // 前台服务启动器
    private lateinit var foregroundLauncher: ForegroundServiceLauncher
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        
        // 初始化通知管理器
        notificationManager = ScanNotificationManager(this)
        notificationManager.initialize()
        
        // 初始化前台服务启动器
        foregroundLauncher = ForegroundServiceLauncher(this)
        
        // 标记服务已启动
        ScanServiceStateManager.markServiceStarted()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动前台服务
        foregroundLauncher.startForeground(
            notificationManager.getNotificationId(),
            notificationManager.getNotification()
        )
        
        // 必须用 START_STICKY 模式保持服务活跃
        return START_STICKY
    }

    override fun onDestroy() {
        // 标记服务已停止
        ScanServiceStateManager.markServiceStopped()
        super.onDestroy()
    }
}