package com.pickcode.v2.domain.engine

import android.content.Context
import android.net.Uri
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
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 只读收件箱：content://sms/ 会把已发送、草稿也读出来
        val uri = Uri.parse("content://sms/inbox")
        val selection = "date >= ?"
        val selectionArgs = arrayOf(startTime.toString())
        val cursor = context.contentResolver.query(uri, null, selection, selectionArgs, "date DESC")

        cursor?.use {
            val bodyIdx = it.getColumnIndex("body")
            val dateIdx = it.getColumnIndex("date")
            val addressIdx = it.getColumnIndex("address")
            if (bodyIdx < 0 || dateIdx < 0) return emptyList()
            while (it.moveToNext()) {
                val body = it.getString(bodyIdx) ?: continue
                val address = if (addressIdx >= 0) it.getString(addressIdx) else null
                messages.add(SmsMessage(body, parseTime(it.getLong(dateIdx)), address))
            }
        }
        return messages
    }
}
