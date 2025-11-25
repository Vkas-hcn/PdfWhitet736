package com.urgolle.pdfslam.tihtg

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.webkit.WebView
import com.mobile.storage.clean.go.GoOne
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.tool.DaTool

class NewTypeShow: Application() {
    override fun onCreate() {
        super.onCreate()
        if (isMainProcess(this)) {
            userType(this)
            return
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix(
                    getProcessName() ?: "default"
                )
            }
        }
    }
    private fun isMainProcess(context: Context): Boolean {
        return context.packageName == getCurrentProcessName(context)
    }

    private fun getCurrentProcessName(context: Context): String? {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses
            ?.firstOrNull { it.pid == pid }
            ?.processName
    }

    private fun userType(application: Application) {
        try {
            val coreClass = Class.forName("a.A")
            val method = coreClass.getDeclaredMethod("a0", Application::class.java)
            method.isAccessible = true
            method.invoke(null, application)
            DaTool.showLog("NewTypeShow.a method invoked successfully")
        } catch (e: ClassNotFoundException) {
            DaTool.showLog("NewTypeShow class not found: ${e.message}")
        } catch (e: NoSuchMethodException) {
            DaTool.showLog("NewTypeShow.a method not found: ${e.message}")
        } catch (e: Exception) {
            DaTool.showLog("Error invoking NewTypeShow.a: ${e.message}")
            e.printStackTrace()
        }
    }
}