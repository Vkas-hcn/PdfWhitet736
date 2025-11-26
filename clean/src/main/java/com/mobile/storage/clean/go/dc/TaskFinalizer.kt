package com.mobile.storage.clean.go.dc

import android.app.Application

// 任务终结器
internal object TaskFinalizer {
    
    fun finalize(app: Application) {
        val tasks = TaskAssembler.assemble(app)
        tasks.forEach { it.invoke() }
    }
}
