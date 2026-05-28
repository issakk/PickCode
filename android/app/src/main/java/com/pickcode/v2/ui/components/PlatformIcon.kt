package com.pickcode.v2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.pickcode.v2.R

@Composable
fun PlatformIcon(platform: String, modifier: Modifier = Modifier) {
    val resId = when (platform) {
        "淘宝" -> R.drawable.taobao
        "京东" -> R.drawable.jd
        "拼多多" -> R.drawable.pinduoduo
        "抖音" -> R.drawable.douyin
        "唯品会" -> R.drawable.weipinhui
        "小红书" -> R.drawable.xiaohongshu
        "1688" -> R.drawable.platform_1688
        else -> R.drawable.other
    }
    Image(painter = painterResource(resId), contentDescription = platform, modifier = modifier)
}
