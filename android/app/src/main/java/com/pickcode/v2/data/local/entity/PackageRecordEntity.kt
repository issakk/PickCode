package com.pickcode.v2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "package_records")
data class PackageRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platform: String,
    val name: String,
    val price: String,
    val time: String,
    val checked: Boolean = false,
    val date: String
)
