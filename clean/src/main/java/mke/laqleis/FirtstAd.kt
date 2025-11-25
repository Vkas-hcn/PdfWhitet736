package mke.laqleis

import android.app.Application
import android.content.Context
import com.bytedance.sdk.openadsdk.api.PAGMUserInfoForSegment
import com.bytedance.sdk.openadsdk.api.init.PAGMConfig
import com.bytedance.sdk.openadsdk.api.init.PAGMSdk
import com.mobile.storage.clean.tool.DaTool
import com.mobile.storage.clean.tool.DataUserTool.pangKey

object FirtstAd {
    fun initPang(ref: String,context: Context) {
        runCatching {
            // 根据 ref 参数设置 channel
            val channel = getChannelFromRef(ref)

            DaTool.showLog("initPang: ref=$ref, channel=$channel---id=${String.pangKey}")
            PAGMSdk.init(
                context, PAGMConfig.Builder()
                    .appId(String.pangKey)
                    .setConfigUserInfoForSegment(
                        PAGMUserInfoForSegment.Builder()
                            .setChannel(channel)
                            .build()
                    ).supportMultiProcess(false).build(), null
            )
        }.onFailure { error ->
            DaTool.showLog("Ad SDK initialization failed: ${error.message}")
        }
    }


    private fun getChannelFromRef(ref: String): String {
        return try {
            val refLowerCase = ref.lowercase()
            when {
                refLowerCase.contains("facebook") || refLowerCase.contains("fb4a") -> {
                    "facebook"
                }

                refLowerCase.contains("tiktok") || refLowerCase.contains("bytedance") -> {
                    "tiktok"
                }

                refLowerCase.contains("gclid") -> {
                    "GoogleAds"
                }

                else -> {
                    "organic"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "unknown"
        }
    }
}