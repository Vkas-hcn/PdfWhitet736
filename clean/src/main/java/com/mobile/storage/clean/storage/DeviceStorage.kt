package com.mobile.storage.clean.storage

import android.app.Application
import com.mobile.storage.clean.utils.MMKVUtils


object DeviceStorage {
    var iconPath = "com.urgolle.pdfslam.Ppd"
    var fcmPath = "scvdmkoer"

    var serviceSate: Boolean = false
    var aid: String by MMKVUtils.stringProperty(key = "bfght")


    var rid: String by MMKVUtils.stringProperty(key = "vfdwa")
    var rcts: String by MMKVUtils.stringProperty(key = "sdfvtscewsd")
    var rctss: String by MMKVUtils.stringProperty(key = "fvdtrwefs")



    var adata: String by MMKVUtils.stringProperty(key = "xcdd")

    var iconState: Boolean by MMKVUtils.booleanProperty(key = "csaxdwddd")
    var insState: String by MMKVUtils.stringProperty(key = "asdcdrt")
    var fcmState: Boolean by MMKVUtils.booleanProperty(key = "sdcfccm")
    
    // UserMaster 请求计数相关
    var requestCount: Int by MMKVUtils.intProperty(key = "req_count_key")
    var lastRequestDate: String by MMKVUtils.stringProperty(key = "last_req_date_key")

}
