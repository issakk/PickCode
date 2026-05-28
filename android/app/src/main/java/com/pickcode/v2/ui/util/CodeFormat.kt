package com.pickcode.v2.ui.util

fun formatCode(code: String): String {
    if (code.length != 8 || !code.all { it.isDigit() }) return code
    return "${code.take(4)} ${code.drop(4)}"
}

fun getCompanyShortName(company: String): String {
    if (company.isEmpty()) return ""
    val firstChar = company.first()
    if (firstChar.code in 0x41..0x7A || firstChar.code in 0x61..0x7A) {
        val word = company.split(" ").firstOrNull() ?: return company.take(2)
        return word.take(2).uppercase()
    }
    return company.take(2)
}
