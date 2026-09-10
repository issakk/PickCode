package com.pickcode.v2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val SuccessGreen = Color(0xFF16A34A)
val SuccessGreenDark = Color(0xFF4ADE80)

/** 成功色（已取/匹配成功）：深色背景下自动换亮一档，否则对比度不够。 */
@Composable
fun successColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) SuccessGreen else SuccessGreenDark
