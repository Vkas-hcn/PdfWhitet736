package com.mobile.storage.clean.tool

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * 保活工具类 - 使用 WorkManager 实现应用保活
 */
object AutoWorkTool {

    private const val LOOP_WORK_NAME = "loop_keep_alive_work"
    private const val PERIODIC_WORK_NAME = "periodic_keep_alive_work"

    /**
     * 循环任务 Worker - 任务完成后启动下一个任务
     */
    class LoopKeepAliveWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {
        
        override fun doWork(): Result {
            // 保活逻辑占位，不需要具体实现
            try {
                // 模拟一些轻量级操作
                Thread.sleep(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 任务完成后，立即启动下一个循环任务
            scheduleNextLoopWork(applicationContext)
            
            return Result.success()
        }
    }

    /**
     * 定期任务 Worker - 由系统定期调度
     */
    class PeriodicKeepAliveWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {
        
        override fun doWork(): Result {
            // 保活逻辑占位，不需要具体实现
            try {
                // 模拟一些轻量级操作
                Thread.sleep(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return Result.success()
        }
    }

    /**
     * 启动保活任务
     */
    fun startKeepAliveWork(context: Context) {
        startLoopWork(context)
        startPeriodicWork(context)
    }

    /**
     * 启动循环任务
     */
    private fun startLoopWork(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<LoopKeepAliveWorker>()
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                LOOP_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
    }

    /**
     * 调度下一个循环任务
     */
    private fun scheduleNextLoopWork(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<LoopKeepAliveWorker>()
            .setInitialDelay(15, TimeUnit.MINUTES) // 15分钟后执行下一次
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                LOOP_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    /**
     * 启动定期任务
     */
    private fun startPeriodicWork(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicKeepAliveWorker>(
            15, TimeUnit.MINUTES // 每15分钟执行一次
        )
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
    }

    /**
     * 取消所有保活任务
     */
    fun cancelKeepAliveWork(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(LOOP_WORK_NAME)
            cancelUniqueWork(PERIODIC_WORK_NAME)
        }
    }
}