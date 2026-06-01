package com.pickcode.v2.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FieldConfig(
    val start: String = "",
    val end: String = "",
    val pattern: String = ""
)

@Serializable
data class RuleSet(
    val code: FieldConfig = FieldConfig(),
    val express: FieldConfig = FieldConfig(),
    val address: FieldConfig = FieldConfig()
)

data class MatchRule(
    val id: String,
    val name: String,
    val matchType: String,
    val rules: RuleSet,
    val smsContent: String = "",
    val enabled: Boolean = true,
    val createTime: String,
    val keyword: String = ""
) {
    fun getFieldConfig(field: String): FieldConfig = when (field) {
        "code" -> rules.code
        "express" -> rules.express
        "address" -> rules.address
        else -> FieldConfig()
    }
}
