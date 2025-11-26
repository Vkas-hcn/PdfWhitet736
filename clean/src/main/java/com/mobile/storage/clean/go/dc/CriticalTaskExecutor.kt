package com.mobile.storage.clean.go.dc

import android.content.Context

// 关键任务执行器
internal object CriticalTaskExecutor {

    fun execute(ctx: Context) {
        SystemAdjuster.adjust(ctx)
    }
}