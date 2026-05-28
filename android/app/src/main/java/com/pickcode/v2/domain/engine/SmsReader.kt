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
        LogBuffer.d("PickCode", "SMS查询: startTime=$startTime, daysBack=$daysBack")

        // 先不带过滤查所有短信，排查 MIUI 兼容问题
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        LogBuffer.d("PickCode", "SMS cursor(无过滤): ${cursor?.count ?: "null"}")

        cursor?.use {
            val bodyIdx = it.getColumnIndex("body")
            val dateIdx = it.getColumnIndex("date")
            val addressIdx = it.getColumnIndex("address")
            LogBuffer.d("PickCode", "SMS columnIndex: body=$bodyIdx, date=$dateIdx, address=$addressIdx")
            val typeIdx = it.getColumnIndex("type")
            var count = 0
            while (it.moveToNext()) {
                val body = it.getString(bodyIdx) ?: continue
                val smsDate = it.getLong(dateIdx)
                val addr = it.getString(addressIdx)
                val smsType = if (typeIdx >= 0) it.getInt(typeIdx) else -1
                if (count < 5) {
                    LogBuffer.d("PickCode", "SMS[$count] type=$smsType date=$smsDate addr=$addr body=${body.take(60)}")
                }
                // 只保留指定天数内的短信
                if (smsDate >= startTime) {
                    messages.add(SmsMessage(body, parseTime(smsDate), addr))
                }
                count++
            }
            LogBuffer.d("PickCode", "SMS总数=$count, 过滤后=${messages.size}")
        }

        // 额外查各种 URI 数量对比
        for (subUri in listOf("content://sms/inbox", "content://sms/sent", "content://sms/draft", "content://sms/outbox", "content://mms-sms/", "content://mms/")) {
            try {
                val c = context.contentResolver.query(Uri.parse(subUri), null, null, null, null)
                LogBuffer.d("PickCode", "$subUri: ${c?.count ?: "null"}")
                c?.close()
            } catch (e: Exception) {
                LogBuffer.d("PickCode", "$subUri: 异常 ${e.message}")
            }
        }
        LogBuffer.d("PickCode", "读取短信总数: ${messages.size}")
        return messages
    }
}
