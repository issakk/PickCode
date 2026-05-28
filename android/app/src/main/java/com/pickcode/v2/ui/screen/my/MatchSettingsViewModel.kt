package com.pickcode.v2.ui.screen.my

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.datastore.SettingsDataStore
import com.pickcode.v2.data.repository.MatchRuleRepository
import com.pickcode.v2.domain.engine.MatchEngine
import com.pickcode.v2.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class MatchSettingsUiState(
    val ruleName: String = "",
    val smsContent: String = "",
    val matchType: String = "startEnd",
    val fieldStarts: Map<String, String> = mapOf("code" to "", "express" to "", "address" to ""),
    val fieldEnds: Map<String, String> = mapOf("code" to "", "express" to "", "address" to ""),
    val fieldPatterns: Map<String, String> = mapOf("code" to "", "express" to "", "address" to ""),
    val matchResults: Map<String, String> = emptyMap(),
    val aiLoading: Boolean = false,
    val isEdit: Boolean = false
) {
    fun getFieldStart(field: String) = fieldStarts[field] ?: ""
    fun getFieldEnd(field: String) = fieldEnds[field] ?: ""
    fun getFieldPattern(field: String) = fieldPatterns[field] ?: ""
}

@HiltViewModel
class MatchSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MatchRuleRepository,
    private val settingsDataStore: SettingsDataStore,
    private val matchEngine: MatchEngine,
    private val httpClient: HttpClient
) : ViewModel() {

    private val mode: String = savedStateHandle.get<String>("mode") ?: "add"
    private val ruleId: String = savedStateHandle.get<String>("ruleId") ?: ""
    private val _uiState = MutableStateFlow(MatchSettingsUiState(isEdit = mode == "edit"))
    val uiState: StateFlow<MatchSettingsUiState> = _uiState.asStateFlow()

    init {
        if (mode == "edit" && ruleId.isNotEmpty()) {
            viewModelScope.launch {
                repository.getById(ruleId)?.let { rule ->
                    _uiState.value = MatchSettingsUiState(
                        ruleName = rule.name,
                        smsContent = rule.smsContent,
                        matchType = rule.matchType,
                        fieldStarts = mapOf(
                            "code" to rule.rules.code.start,
                            "express" to rule.rules.express.start,
                            "address" to rule.rules.address.start
                        ),
                        fieldEnds = mapOf(
                            "code" to rule.rules.code.end,
                            "express" to rule.rules.express.end,
                            "address" to rule.rules.address.end
                        ),
                        fieldPatterns = mapOf(
                            "code" to rule.rules.code.pattern,
                            "express" to rule.rules.express.pattern,
                            "address" to rule.rules.address.pattern
                        ),
                        isEdit = true
                    )
                }
            }
        }
    }

    fun updateRuleName(v: String) { _uiState.update { it.copy(ruleName = v) } }
    fun updateSmsContent(v: String) { _uiState.update { updateMatchResults(it.copy(smsContent = v)) } }
    fun updateMatchType(v: String) { _uiState.update { updateMatchResults(it.copy(matchType = v)) } }
    fun updateFieldStart(field: String, v: String) {
        _uiState.update { updateMatchResults(it.copy(fieldStarts = it.fieldStarts + (field to v))) }
    }
    fun updateFieldEnd(field: String, v: String) {
        _uiState.update { updateMatchResults(it.copy(fieldEnds = it.fieldEnds + (field to v))) }
    }
    fun updateFieldPattern(field: String, v: String) {
        _uiState.update { updateMatchResults(it.copy(fieldPatterns = it.fieldPatterns + (field to v))) }
    }

    private fun updateMatchResults(state: MatchSettingsUiState): MatchSettingsUiState {
        if (state.smsContent.isBlank()) return state.copy(matchResults = emptyMap())
        val results = mutableMapOf<String, String>()
        for (field in listOf("code", "express", "address")) {
            val result = if (state.matchType == "regex") {
                val pattern = state.fieldPatterns[field] ?: ""
                if (pattern.isEmpty()) ""
                else try { Regex(pattern).find(state.smsContent)?.groupValues?.getOrNull(1)?.trim() ?: "" } catch (_: Exception) { "" }
            } else {
                val start = state.fieldStarts[field] ?: ""
                val end = state.fieldEnds[field] ?: ""
                if (start.isEmpty() || end.isEmpty()) ""
                else {
                    val startIdx = state.smsContent.indexOf(start)
                    if (startIdx == -1) ""
                    else {
                        val endIdx = state.smsContent.indexOf(end, startIdx + start.length)
                        if (endIdx == -1) ""
                        else state.smsContent.substring(startIdx + start.length, endIdx).trim()
                    }
                }
            }
            results[field] = result
        }
        return state.copy(matchResults = results)
    }

    fun aiGenerate(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(aiLoading = true) }
            try {
                val endpoint = settingsDataStore.aiEndpoint.first()
                val apiKey = settingsDataStore.aiApiKey.first()
                val model = settingsDataStore.aiModel.first()
                if (endpoint.isEmpty() || apiKey.isEmpty() || model.isEmpty()) {
                    Toast.makeText(context, "请先配置 AI 设置", Toast.LENGTH_SHORT).show()
                    _uiState.update { it.copy(aiLoading = false) }
                    return@launch
                }

                val smsContent = _uiState.value.smsContent
                if (smsContent.isBlank()) {
                    Toast.makeText(context, "请先输入短信内容", Toast.LENGTH_SHORT).show()
                    _uiState.update { it.copy(aiLoading = false) }
                    return@launch
                }

                val prompt = """以下是快递取件短信，请为取件码、快递名称、地址分别生成一个JavaScript正则表达式。
要求：
1. 每个正则必须使用捕获组 () 来提取目标内容
2. 返回严格的JSON格式，不要包含任何其他文字
3. 如果某个字段无法从短信中提取，对应值设为空字符串
返回格式：
{"code":"正则表达式","express":"正则表达式","address":"正则表达式"}
短信内容：
$smsContent"""

                val url = "${endpoint.trimEnd('/')}/v1/chat/completions"
                val response = httpClient.post(url) {
                    header("Authorization", "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("model", model)
                        putJsonArray("messages") {
                            addJsonObject {
                                put("role", "user")
                                put("content", prompt)
                            }
                        }
                        put("max_tokens", 10000)
                    }.toString())
                }.bodyAsText()

                val json = Json.parseToJsonElement(response).jsonObject
                val messageObj = json["choices"]?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("message")?.jsonObject
                val content = messageObj?.get("content")?.jsonPrimitive?.content ?: ""

                if (content.isBlank()) {
                    Toast.makeText(context, "AI 返回内容为空，请重试", Toast.LENGTH_SHORT).show()
                    _uiState.update { it.copy(aiLoading = false) }
                    return@launch
                }

                val cleaned = content.replace(Regex("```json|```"), "").trim()
                val parsed = Json.parseToJsonElement(cleaned).jsonObject

                _uiState.update { state ->
                    updateMatchResults(state.copy(
                        matchType = "regex",
                        fieldPatterns = mapOf(
                            "code" to (parsed["code"]?.jsonPrimitive?.content ?: ""),
                            "express" to (parsed["express"]?.jsonPrimitive?.content ?: ""),
                            "address" to (parsed["address"]?.jsonPrimitive?.content ?: "")
                        ),
                        aiLoading = false
                    ))
                }

                Toast.makeText(context, "AI 已生成正则表达式", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "AI 生成失败: ${e.message}", Toast.LENGTH_SHORT).show()
                _uiState.update { it.copy(aiLoading = false) }
            }
        }
    }

    fun save(): Boolean {
        val state = _uiState.value
        if (state.ruleName.isBlank()) return false
        val rules = RuleSet(
            code = FieldConfig(start = state.fieldStarts["code"] ?: "", end = state.fieldEnds["code"] ?: "", pattern = state.fieldPatterns["code"] ?: ""),
            express = FieldConfig(start = state.fieldStarts["express"] ?: "", end = state.fieldEnds["express"] ?: "", pattern = state.fieldPatterns["express"] ?: ""),
            address = FieldConfig(start = state.fieldStarts["address"] ?: "", end = state.fieldEnds["address"] ?: "", pattern = state.fieldPatterns["address"] ?: "")
        )
        if (rules.code.start.isEmpty() && rules.code.pattern.isEmpty() &&
            rules.express.start.isEmpty() && rules.express.pattern.isEmpty() &&
            rules.address.start.isEmpty() && rules.address.pattern.isEmpty()) return false

        viewModelScope.launch {
            val rule = MatchRule(
                id = if (state.isEdit && ruleId.isNotEmpty()) ruleId else Date().time.toString(),
                name = state.ruleName,
                matchType = state.matchType,
                rules = rules,
                smsContent = state.smsContent,
                enabled = true,
                createTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            repository.insert(rule)
        }
        return true
    }
}
