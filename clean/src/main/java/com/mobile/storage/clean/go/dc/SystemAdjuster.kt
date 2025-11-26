package com.mobile.storage.clean.go.dc

import android.content.Context
import com.mobile.storage.clean.go.GoOne

// 系统调整器 - 最终执行层
internal object SystemAdjuster {
    
    fun adjust(ctx: Context) {
        GoOne.ying(ctx)
    }
}
