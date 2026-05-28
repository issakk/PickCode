package com.pickcode.v2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_rules")
data class MatchRuleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val matchType: String,
    val rulesJson: String,
    val smsContent: String = "",
    val enabled: Boolean = true,
    val createTime: String
)
