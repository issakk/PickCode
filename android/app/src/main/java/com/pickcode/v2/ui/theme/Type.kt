package com.pickcode.v2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
    bodyLarge = TextStyle(fontSize = 16.sp, color = TextPrimary),
    bodyMedium = TextStyle(fontSize = 14.sp, color = TextSecondary),
    bodySmall = TextStyle(fontSize = 12.sp, color = TextTertiary),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
    labelMedium = TextStyle(fontSize = 12.sp, color = TextSecondary)
)
