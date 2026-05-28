package com.pickcode.v2.domain.model

data class PackageRecord(
    val id: Long = 0,
    val platform: String,
    val name: String,
    val price: String,
    val time: String,
    val checked: Boolean = false,
    val date: String
)
