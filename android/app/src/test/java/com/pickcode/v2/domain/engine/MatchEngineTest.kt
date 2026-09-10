package com.pickcode.v2.domain.engine

import com.pickcode.v2.domain.model.FieldConfig
import com.pickcode.v2.domain.model.MatchRule
import com.pickcode.v2.domain.model.RuleSet
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchEngineTest {

    private val engine = MatchEngine()

    private fun rule(
        matchType: String = "startEnd",
        code: FieldConfig = FieldConfig(),
        express: FieldConfig = FieldConfig(),
        address: FieldConfig = FieldConfig(),
        keyword: String = ""
    ) = MatchRule(
        id = "t",
        name = "t",
        matchType = matchType,
        rules = RuleSet(code = code, express = express, address = address),
        createTime = "",
        keyword = keyword
    )

    @Test
    fun startEndExtractsEveryCode() {
        val sms = "凭A-1-1001的包裹已到菜鸟驿站，凭A-2-1002的包裹已到菜鸟驿站"
        val info = engine.extractInfo(
            sms,
            listOf(
                rule(
                    code = FieldConfig(start = "凭", end = "的"),
                    express = FieldConfig(start = "的", end = "已到"),
                    address = FieldConfig(start = "已到", end = "，")
                )
            )
        )
        assertEquals(listOf("A-1-1001", "A-2-1002"), info.codes)
        assertEquals("包裹", info.express)
        assertEquals("菜鸟驿站", info.address)
    }

    @Test
    fun startEndDropsDuplicateCodes() {
        val sms = "取件码11-1-1111，取件码11-1-1111"
        val info = engine.extractInfo(
            sms,
            listOf(rule(code = FieldConfig(start = "取件码", end = "，")))
        )
        assertEquals(listOf("11-1-1111"), info.codes)
    }

    @Test
    fun regexExtractsEveryCode() {
        val sms = "取件码 40-2-4693 取件码 40-2-3225"
        val info = engine.extractInfo(
            sms,
            listOf(rule(matchType = "regex", code = FieldConfig(pattern = "取件码\\s*(\\d+-\\d+-\\d+)")))
        )
        assertEquals(listOf("40-2-4693", "40-2-3225"), info.codes)
    }

    @Test
    fun keywordSkipsUnrelatedSms() {
        val sms = "凭A-1-1001的包裹已到菜鸟驿站"
        val info = engine.extractInfo(
            sms,
            listOf(rule(keyword = "丰巢", code = FieldConfig(start = "凭", end = "的")))
        )
        assertEquals(emptyList<String>(), info.codes)
    }

    @Test
    fun emptyAndInvalidConfigNeverCrash() {
        val sms = "凭A-1-1001的包裹已到菜鸟驿站"
        assertEquals(emptyList<String>(), engine.extractInfo(sms, listOf(rule())).codes)
        assertEquals(
            emptyList<String>(),
            engine.extractInfo(sms, listOf(rule(matchType = "regex", code = FieldConfig(pattern = "(")))).codes
        )
        assertEquals(
            engine.extractInfo(sms, emptyList()).codes,
            engine.extractInfo(sms, listOf(rule(enabled = false, code = FieldConfig(start = "凭", end = "的")))).codes
        )
    }
}
