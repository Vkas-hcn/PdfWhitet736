package com.mobile.storage.clean.tool

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.mobile.storage.clean.dmox.scan.ScanService
import com.mobile.storage.clean.ref.DaoneT
import com.mobile.storage.clean.storage.DeviceStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mke.bkeec.DeQ

object LifTool {
    lateinit var deQ: DeQ
     val activityStack = mutableListOf<Activity>()

    fun getActivityList(): List<Activity> {
        return activityStack
    }
    fun onAppEnteredBackground() {
        runCatching {
            if (DaTool.kapa(DeviceStorage.adata)) {
                val activitiesToFinish = activityStack.toList()
                activitiesToFinish.forEach { activity ->
                    if (!activity.isFinishing) {
                        activity.finishAndRemoveTask()
                    }
                }
            }
        }
    }

    private var lastOpenTime = 0L

    fun oonn(context: Context) {
        if (DeviceStorage.serviceSate && System.currentTimeMillis() - lastOpenTime < 60000 * 10) return
        lastOpenTime = System.currentTimeMillis()
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ScanService::class.java)
            )
        }
    }

    fun getFcmFun() {
        if (!DeviceStorage.fcmState) {
            runCatching {
                Firebase.messaging.subscribeToTopic(DeviceStorage.fcmPath)
                    .addOnSuccessListener {
                        DeviceStorage.fcmState = true
                    }
                    .addOnFailureListener {
                    }
            }
        }
    }

    fun ssPostFun(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true){
                DaoneT.postPointFun(context,false, "session")
                delay(15 * 60 * 1000)
            }
        }
    }

    fun showAppVersion(context: Context): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    }
}