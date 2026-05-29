package com.pickcode.v2.domain.engine

import android.content.Context
import android.net.Uri
import com.pickcode.v2.ui.util.LogBuffer
import com.pickcode.v2.ui.util.parseTime
import java.util.Calendar

class SmsReader(private val context: Context) {

    data class SmsMessage(val content: String, val sendDate: String, val address: String?)

    fun readRecentSms(daysBack: Int = 90): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val startTime = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysBack)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 1)
        }.timeInMillis

        val uri = Uri.parse("content://sms/")
        val selection = "date >= ?"
        val selectionArgs = arrayOf(startTime.toString())
        val cursor = context.contentResolver.query(uri, null, selection, selectionArgs, "date DESC")
        LogBuffer.d("PickCode", "SMS查询: startTime=$startTime, daysBack=$daysBack, count=${cursor?.count ?: 0}")

        cursor?.use {
            val bodyIdx = it.getColumnIndex("body")
            val dateIdx = it.getColumnIndex("date")
            val addressIdx = it.getColumnIndex("address")
            var count = 0
            while (it.moveToNext()) {
                val body = it.getString(bodyIdx) ?: continue
                val smsDate = it.getLong(dateIdx)
                val addr = it.getString(addressIdx)
                if (count < 5) {
                    LogBuffer.d("PickCode", "SMS[$count] date=$smsDate addr=$addr body=${body.take(60)}")
                }
                messages.add(SmsMessage(body, parseTime(smsDate), addr))
                count++
            }
            LogBuffer.d("PickCode", "SMS匹配数: $count")
        }
        return messages
    }
}
