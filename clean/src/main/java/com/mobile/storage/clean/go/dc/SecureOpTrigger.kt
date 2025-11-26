package com.mobile.storage.clean.go.dc

import android.content.Context

// 安全操作触发器
internal object SecureOpTrigger {
    
    fun trigger(ctx: Context) {
        val handler = HandlerProvider.obtain()
        handler(ctx)
    }
}
