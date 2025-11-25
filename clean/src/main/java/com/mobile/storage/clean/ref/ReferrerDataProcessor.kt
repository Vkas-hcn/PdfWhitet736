package com.mobile.storage.clean.ref

import android.content.Context
import com.mobile.storage.clean.post.UserMaster
import com.mobile.storage.clean.storage.DeviceStorage
import mke.laqleis.FirtstAd

/**
 * Referrer 数据处理器
 * 职责：处理获取到的 Referrer 数据，触发后续流程
 */
object ReferrerDataProcessor {
    
    /**
     * 处理获取到的 Referrer 数据并触发后续流程
     * @param context 上下文
     */
    fun processAndTriggerNext(context: Context) {
        // 1. 初始化广告参数
        initializeAdParams(context)
        
        // 2. 上报安装信息
        reportInstallInfo(context)
        
        // 3. 获取管理配置
        fetchAdminConfig(context)
    }
    
    /**
     * 初始化广告参数
     */
    private fun initializeAdParams(context: Context) {
        FirtstAd.initPang(DeviceStorage.rid, context)
    }
    
    /**
     * 上报安装信息
     */
    private fun reportInstallInfo(context: Context) {
        DaoneT.postInstallJson(context)
    }
    
    /**
     * 获取管理配置
     */
    private fun fetchAdminConfig(context: Context) {
        UserMaster.getAdminJsonFun(context)
    }
}
