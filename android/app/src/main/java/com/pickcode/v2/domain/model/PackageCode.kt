package com.pickcode.v2.domain.model

data class PackageCode(
    val id: Long = 0,
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
