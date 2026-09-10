package com.pickcode.v2.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "package_codes",
    indices = [Index(value = ["code", "date"], unique = true)]
)
data class PackageCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val date: String,
    val sendDate: String,
    val company: String,
    val address: String,
    val isPicked: Boolean = false,
    val isManual: Boolean = false,
    val tags: List<String> = emptyList(),
    val remark: String = ""
)
