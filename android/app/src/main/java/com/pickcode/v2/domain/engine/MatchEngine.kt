package com.pickcode.v2.domain.engine

import com.pickcode.v2.domain.model.MatchRule

class MatchEngine {

    data class ExtractedInfo(
        val codes: List<String> = emptyList(),
        val express: String = "",
        val address: String = ""
    )

    fun extractInfo(content: String, rules: List<MatchRule>): ExtractedInfo {
        var info = ExtractedInfo()
        val enabledRules = rules.filter { it.enabled }

        for (rule in enabledRules) {
            for (field in listOf("code", "express", "address")) {
                val currentValue = when (field) {
                    "code" -> info.codes.isNotEmpty()
                    "express" -> info.express
                    else -> info.address
                }
                if (currentValue is String && currentValue.isNotEmpty()) continue
                if (currentValue is Boolean && currentValue) continue

                val fieldConfig = rule.getFieldConfig(field)
                val results = if (rule.matchType == "regex") {
                    matchRegexAll(content, fieldConfig.pattern)
                } else {
                    matchStartEndAll(content, fieldConfig.start, fieldConfig.end)
                }

                if (results.isNotEmpty()) {
                    info = when (field) {
                        "code" -> info.copy(codes = results)
                        "express" -> info.copy(express = results.first())
                        else -> info.copy(address = results.first())
                    }
                }
            }
            if (info.codes.isNotEmpty() && info.express.isNotEmpty() && info.address.isNotEmpty()) break
        }
        return info
    }

    private fun matchStartEndAll(content: String, start: String, end: String): List<String> {
        if (start.isEmpty() || end.isEmpty()) return emptyList()
        val startIdx = content.indexOf(start)
        if (startIdx == -1) return emptyList()
        val endIdx = content.indexOf(end, startIdx + start.length)
        if (endIdx == -1) return emptyList()
        return listOf(content.substring(startIdx + start.length, endIdx).trim())
    }

    private fun matchRegexAll(content: String, pattern: String): List<String> {
        if (pattern.isEmpty()) return emptyList()
        return try {
            val regex = Regex(pattern)
            regex.findAll(content).mapNotNull { match ->
                match.groupValues.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
            }.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
