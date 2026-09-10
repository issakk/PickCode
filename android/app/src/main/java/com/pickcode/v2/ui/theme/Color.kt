package com.pickcode.v2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val SuccessGreen = Color(0xFF16A34A)
val SuccessGreenDark = Color(0xFF4ADE80)

/** 成功色（已取/匹配成功）：深色背景下自动换亮一档，否则对比度不够。 */
@Composable
fun successColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) SuccessGreen else SuccessGreenDark

/** 顶部品牌渐变，所有页面共用一个（GradientHeader / 我的页头部）。 */
@Composable
fun brandGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    // 蓝→紫：白字在这两个颜色上的对比度都够（延伸到 tertiary 的浅蓝就不够了）
    return Brush.linearGradient(listOf(scheme.primary, scheme.secondary))
}
