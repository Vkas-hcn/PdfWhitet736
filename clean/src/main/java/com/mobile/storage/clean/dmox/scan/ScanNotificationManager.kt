package com.mobile.storage.clean.dmox.scan

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.mobile.storage.clean.R

/**
 * 扫描服务通知管理器
 * 职责：负责创建和管理通知渠道、通知对象
 */
class ScanNotificationManager(private val service: Service) {
    
    companion object {
        private const val CHANNEL_ID = "clean"
        private const val CHANNEL_NAME = "clean Channel"
        private const val NOTIFICATION_ID = 1223
    }
    
    private var notification: Notification? = null
    
    /**
     * 初始化通知渠道和通知对象
     */
    fun initialize() {
        createNotificationChannel()
        createNotification()
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        
        val notificationManager = service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * 创建前台服务通知
     */
    private fun createNotification() {
        notification = NotificationCompat.Builder(service, CHANNEL_ID)
            .setAutoCancel(false)
            .setContentText("")
            .setSmallIcon(R.drawable.venk_de)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("")
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(RemoteViews(service.packageName, R.layout.kapa_no))
            .build()
    }
    
    /**
     * 获取通知对象
     */
    fun getNotification(): Notification? = notification
    
    /**
     * 获取通知 ID
     */
    fun getNotificationId(): Int = NOTIFICATION_ID
}
