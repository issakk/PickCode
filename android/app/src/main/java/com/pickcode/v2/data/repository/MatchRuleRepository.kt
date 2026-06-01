package com.pickcode.v2.data.repository

import com.pickcode.v2.data.local.dao.MatchRuleDao
import com.pickcode.v2.data.local.entity.MatchRuleEntity
import com.pickcode.v2.domain.model.MatchRule
import com.pickcode.v2.domain.model.RuleSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRuleRepository @Inject constructor(
    private val dao: MatchRuleDao
) {
    fun getAll(): Flow<List<MatchRule>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getEnabled(): List<MatchRule> = dao.getEnabled().map { it.toDomain() }

    suspend fun getById(id: String): MatchRule? = dao.getById(id)?.toDomain()

    suspend fun insert(rule: MatchRule) = dao.insert(rule.toEntity())

    suspend fun update(rule: MatchRule) = dao.update(rule.toEntity())

    suspend fun delete(rule: MatchRule) = dao.delete(rule.toEntity())

    suspend fun countByName(name: String, excludeId: String = ""): Int =
        dao.countByName(name, excludeId)
}

private fun MatchRuleEntity.toDomain() = MatchRule(
    id = id, name = name, matchType = matchType,
    rules = Json.decodeFromString(rulesJson),
    smsContent = smsContent, enabled = enabled, createTime = createTime, keyword = keyword
)

private fun MatchRule.toEntity() = MatchRuleEntity(
    id = id, name = name, matchType = matchType,
    rulesJson = Json.encodeToString(rules),
    smsContent = smsContent, enabled = enabled, createTime = createTime, keyword = keyword
)
