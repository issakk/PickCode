package com.pickcode.v2.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Domain-specific colors not covered by M3 color scheme
val SuccessGreen = Color(0xFF22C55E)
val OrangePrice = Color(0xFFF97316)

// Extension properties for use via MaterialTheme.colorScheme.success / .specialty
val ColorScheme.success: Color get() = SuccessGreen
val ColorScheme.specialty: Color get() = OrangePrice
