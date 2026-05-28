package com.pickcode.v2.ui.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LogEntry(val tag: String, val message: String, val timestamp: Long = System.currentTimeMillis())

object LogBuffer {
    private const val MAX_SIZE = 500
    private val buffer = ArrayDeque<LogEntry>(MAX_SIZE)
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        synchronized(buffer) {
            if (buffer.size >= MAX_SIZE) buffer.removeFirst()
            buffer.addLast(LogEntry(tag, message))
            _logs.value = buffer.toList()
        }
    }

    fun clear() {
        synchronized(buffer) {
            buffer.clear()
            _logs.value = emptyList()
        }
    }
}
