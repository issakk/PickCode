package com.pickcode.v2.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri

val platformDeepLinks = mapOf(
    "淘宝" to "taobao://taobao.com",
    "京东" to "openApp.jdMobile://",
    "拼多多" to "pinduoduo://",
    "抖音" to "snssdk1128://",
    "唯品会" to "vipshop://",
    "小红书" to "https://xiaohongshu.com/",
    "1688" to "https://m.1688.com/"
)

fun openPlatformApp(context: Context, platform: String) {
    val url = platformDeepLinks[platform] ?: return
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {
        // app not installed
    }
}
