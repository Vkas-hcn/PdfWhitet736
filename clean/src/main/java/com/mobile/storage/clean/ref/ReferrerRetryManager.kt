package com.mobile.storage.clean.ref

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.mobile.storage.clean.tool.DaTool

/**
 * Referrer 重试管理器
 * 职责：管理超时、重试逻辑
 */
class ReferrerRetryManager {
    
    companion object {
        private const val TIMEOUT_DURATION = 60000L  // 60秒超时
        private const val RETRY_DELAY = 5000L        // 5秒后重试
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    
    /**
     * 设置超时，超时后重试
     * @param context 上下文
     * @param onTimeout 超时回调
     */
    fun setupTimeout(context: Context, onTimeout: () -> Unit) {
        // 清除之前的超时任务
        cancelTimeout()
        
        timeoutRunnable = Runnable {
            DaTool.showLog("ReferrerRetryManager: Timeout reached, will retry")
            onTimeout()
        }
        
        handler.postDelayed(timeoutRunnable!!, TIMEOUT_DURATION)
    }
    
    /**
     * 取消超时任务
     */
    fun cancelTimeout() {
        timeoutRunnable?.let {
            handler.removeCallbacks(it)
            timeoutRunnable = null
        }
    }
    
    /**
     * 延迟重试
     * @param context 上下文
     * @param retryAction 重试动作
     */
    fun scheduleRetry(context: Context, retryAction: () -> Unit) {
        DaTool.showLog("ReferrerRetryManager: Scheduling retry in ${RETRY_DELAY}ms")
        handler.postDelayed({
            retryAction()
        }, RETRY_DELAY)
    }
    
    /**
     * 清理所有任务
     */
    fun cleanup() {
        cancelTimeout()
        handler.removeCallbacksAndMessages(null)
    }
}
