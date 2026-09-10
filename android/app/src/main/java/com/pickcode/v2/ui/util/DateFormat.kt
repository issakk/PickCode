package com.pickcode.v2.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun parseTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateChinese(dateStr: String): String {
    return try {
        val date = java.time.LocalDate.parse(dateStr.take(10))
        "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
    } catch (e: Exception) {
        dateStr
    }
}

/** 当前时刻，"yyyy-MM-dd HH:mm:ss"（入库用） */
fun todayString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}
