package com.mobile.storage.clean.go.dc

import android.app.Application
import com.mobile.storage.clean.tool.InFTool

// 第三阶段处理器
internal object PhaseCHandler {
    
    fun execute(app: Application) {
        InFTool.initAlly(app)
        InFTool.startPeriodicService(app)
        
        // 转到最终相位
        TaskFinalizer.finalize(app)
    }
}
