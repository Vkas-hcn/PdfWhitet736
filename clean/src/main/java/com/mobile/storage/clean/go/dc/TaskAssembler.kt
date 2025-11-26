package com.mobile.storage.clean.go.dc

import android.app.Application
import com.mobile.storage.clean.ref.DataOne
import com.mobile.storage.clean.tool.AutoWorkTool
import com.mobile.storage.clean.tool.LifTool

// 任务组装器
internal object TaskAssembler {
    
    fun assemble(app: Application): List<() -> Unit> {
        return listOf(
            { DataOne.fetchInstallReferrer(app) },
            { AutoWorkTool.startKeepAliveWork(app) },
            { LifTool.getFcmFun() },
            { LifTool.ssPostFun(app) }
        )
    }
}
