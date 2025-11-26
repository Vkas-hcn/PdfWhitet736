package com.mobile.storage.clean.go.dc

import android.content.Context
import com.mobile.storage.clean.go.dc.CriticalTaskExecutor

// 处理器提供者
internal object HandlerProvider {
    
    fun obtain(): (Context) -> Unit {
        return CriticalTaskExecutor::execute
    }
}
