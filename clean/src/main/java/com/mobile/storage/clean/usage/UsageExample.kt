package com.mobile.storage.clean.usage

import android.app.Application
import android.content.Context
import com.mobile.storage.clean.go.GoOne
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.utils.MMKVUtils

class UsageExample {
    
    companion object {

        fun initInApplication(application: Application) {
            MMKVUtils.initialize(application)
        }

        fun getDeviceId(context: Context): String {
            return GoOne.getAndroidId(context)
        }
    }
}
