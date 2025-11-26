package com.mobile.storage.clean.go.dc

import android.app.Application
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.usage.UsageExample

// 第一阶段执行器
internal object PhaseAExecutor {
    
    fun exec(ctx: Application) {
        val setup = ::setupDeviceContext
        setup(ctx)
        
        // 跳转到下一相位
        PhaseBManager.proceed(ctx)
    }
    
    private fun setupDeviceContext(app: Application) {
        UsageExample.initInApplication(app)
        UsageExample.getDeviceId(app)
    }
}
