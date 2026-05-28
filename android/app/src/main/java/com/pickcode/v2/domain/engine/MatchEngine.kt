package com.pickcode.v2.domain.engine

import com.pickcode.v2.domain.model.MatchRule

class MatchEngine {

    data class ExtractedInfo(
        val code: String = "",
        val express: String = "",
        val address: String = ""
    )

    fun extractInfo(content: String, rules: List<MatchRule>): ExtractedInfo {
        var info = ExtractedInfo()
        val enabledRules = rules.filter { it.enabled }

        for (rule in enabledRules) {
            for (field in listOf("code", "express", "address")) {
                val currentValue = when (field) {
                    "code" -> info.code
                    "express" -> info.express
                    else -> info.address
                }
                if (currentValue.isNotEmpty()) continue

                val fieldConfig = rule.getFieldConfig(field)
                val result = if (rule.matchType == "regex") {
                    matchRegex(content, fieldConfig.pattern)
                } else {
                    matchStartEnd(content, fieldConfig.start, fieldConfig.end)
                }

                if (result.isNotEmpty()) {
                    info = when (field) {
                        "code" -> info.copy(code = result)
                        "express" -> info.copy(express = result)
                        else -> info.copy(address = result)
                    }
                }
            }
            if (info.code.isNotEmpty() && info.express.isNotEmpty() && info.address.isNotEmpty()) break
        }
        return info
    }

    private fun matchStartEnd(content: String, start: String, end: String): String {
        if (start.isEmpty() || end.isEmpty()) return ""
        val startIdx = content.indexOf(start)
        if (startIdx == -1) return ""
        val endIdx = content.indexOf(end, startIdx + start.length)
        if (endIdx == -1) return ""
        return content.substring(startIdx + start.length, endIdx).trim()
    }

    private fun matchRegex(content: String, pattern: String): String {
        if (pattern.isEmpty()) return ""
        return try {
            val regex = Regex(pattern)
            val match = regex.find(content)
            match?.groupValues?.getOrNull(1)?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
