package com.mobile.storage.clean.tool

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerLib
import com.mobile.storage.clean.dmox.scan.ScanService
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.tool.DataUserTool.applyKey
import mke.bkeec.DeQ
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object InFTool {
    fun initAlly(app: Application) {
        try {

//            DaTool.showLog("initAlly: AF设备ID=${DeviceStorage.aid}---af-id${String.applyKey}")
            AppsFlyerLib.getInstance()
                .init(String.applyKey, null, app)
            AppsFlyerLib.getInstance().setCustomerUserId(DeviceStorage.aid)
            AppsFlyerLib.getInstance().start(app)
        } catch (e: Exception) {
            DaTool.showLog("initAlly failed: ${e.message}")
        }
    }

//    fun testAf() {
//        val adRevenueData = com.appsflyer.AFAdRevenueData(
//            "pangle",
//            com.appsflyer.MediationNetwork.TRADPLUS,
//            "USD",
//            0.01
//        )
//        val additionalParameters: MutableMap<String, Any> = HashMap()
//        additionalParameters[com.appsflyer.AdRevenueScheme.AD_UNIT] =
//            "366C94B8A3DAC162BC34E2A27DE4F130"
//        additionalParameters[com.appsflyer.AdRevenueScheme.AD_TYPE] = "Interstitial"
//        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
//    }


    private val scheduler = Executors.newScheduledThreadPool(1)
    private var scheduledFuture: ScheduledFuture<*>? = null

    fun startPeriodicService(context: Context) {
        stopPeriodicService()
        scheduledFuture = scheduler.scheduleWithFixedDelay({
            if (!DeviceStorage.serviceSate && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, ScanService::class.java)
                )
            }
        }, 0, 1121, TimeUnit.MILLISECONDS)
    }

    fun stopPeriodicService() {
        scheduledFuture?.cancel(false)
        scheduledFuture = null
    }

    fun lifStart(app: Application) {
        LifTool.deQ = DeQ()
        app.registerActivityLifecycleCallbacks(LifTool.deQ)

    }
}