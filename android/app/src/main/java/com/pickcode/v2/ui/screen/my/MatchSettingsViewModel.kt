package com.pickcode.v2.ui.screen.my

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.datastore.SettingsDataStore
import com.pickcode.v2.data.repository.MatchRuleRepository
import com.pickcode.v2.domain.engine.MatchEngine
import com.pickcode.v2.domain.engine.SmsReader
import com.pickcode.v2.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class MatchSettingsUiState(
    val ruleName: String = "",
    val smsContent: String = "",
    val matchType: String = "startEnd",
    val keyword: String = "",
    val fieldStarts: Map<String, String> = mapOf("code" to "", "express" to "", "address" to ""),
    val fieldEnds: Map<String, String> = mapOf("code" to "", "express" to "", "address" to ""),
    val fieldPatterns: Map<String, String> = mapOf("code" to "", "express" to "", "address" to ""),
    val matchResults: Map<String, String> = emptyMap(),
    val aiLoading: Boolean = false,
    val backtestLoading: Boolean = false,
    val backtest: BacktestResult? = null,
    val enabled: Boolean = true,
    val isEdit: Boolean = false
) {
    fun getFieldStart(field: String) = fieldStarts[field] ?: ""
    fun getFieldEnd(field: String) = fieldEnds[field] ?: ""
    fun getFieldPattern(field: String) = fieldPatterns[field] ?: ""
}

/** 回测：这条规则在最近短信里的命中情况 */
data class BacktestSample(
    val sms: String,
    val code: String,
    val express: String,
    val address: String
)

data class BacktestResult(
    val scanned: Int,
    val matched: Int,
    val samples: List<BacktestSample>
)

@HiltViewModel
class MatchSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MatchRuleRepository,
    private val settingsDataStore: SettingsDataStore,
    private val matchEngine: MatchEngine,
    private val smsReader: SmsReader,
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
                    _uiState.value = updateMatchResults(MatchSettingsUiState(
                        ruleName = rule.name,
                        smsContent = rule.smsContent,
                        matchType = rule.matchType,
                        keyword = rule.keyword,
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
                        enabled = rule.enabled,
                        isEdit = true
                    ))
                }
            }
        }
    }

    fun updateRuleName(v: String) { _uiState.update { it.copy(ruleName = v) } }
    fun updateSmsContent(v: String) { _uiState.update { updateMatchResults(it.copy(smsContent = v)) } }
    fun updateMatchType(v: String) { _uiState.update { updateMatchResults(it.copy(matchType = v)) } }
    fun updateKeyword(v: String) { _uiState.update { it.copy(keyword = v) } }
    fun updateFieldStart(field: String, v: String) {
        _uiState.update { updateMatchResults(it.copy(fieldStarts = it.fieldStarts + (field to v))) }
    }
    fun updateFieldEnd(field: String, v: String) {
        _uiState.update { updateMatchResults(it.copy(fieldEnds = it.fieldEnds + (field to v))) }
    }
    fun updateFieldPattern(field: String, v: String) {
        _uiState.update { updateMatchResults(it.copy(fieldPatterns = it.fieldPatterns + (field to v))) }
    }

    /** 预览复用 MatchEngine，避免预览和实际匹配两套逻辑漂移。 */
    private fun updateMatchResults(state: MatchSettingsUiState): MatchSettingsUiState {
        if (state.smsContent.isBlank()) return state.copy(matchResults = emptyMap())
        // 预览不套关键词筛选：改字段时能立刻看到抽取结果
        val rule = buildRule(state, id = "preview", includeKeyword = false)
        val info = matchEngine.extractInfo(state.smsContent, listOf(rule))
        return state.copy(
            matchResults = mapOf(
                "code" to info.codes.joinToString(", "),
                "express" to info.express,
                "address" to info.address
            )
        )
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
1. 每个正则必须使用捕获组 () 来提取目标内容，正则不要包含全局标志（如 /g）
2. 短信可能包含多个取件码（如 A-1-1001, A-2-1002），取件码正则需能匹配到每条短信内容中所有的取件码（例如匹配 40-2-4693 也匹配 40-2-3225）
3. 返回严格的JSON格式，不要包含任何其他文字
4. 如果某个字段无法从短信中提取，对应值设为空字符串
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
                enabled = state.enabled, // 编辑时保留原有的启用状态
                createTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                keyword = state.keyword
            )
            repository.insert(rule)
        }
        return true
    }

    /** 把当前编辑器内容组装成一条 MatchRule，预览和回测共用同一份组装逻辑。 */
    private fun buildRule(
        state: MatchSettingsUiState,
        id: String,
        includeKeyword: Boolean
    ) = MatchRule(
        id = id,
        name = state.ruleName.ifBlank { "未命名规则" },
        matchType = state.matchType,
        rules = RuleSet(
            code = FieldConfig(
                start = state.getFieldStart("code"),
                end = state.getFieldEnd("code"),
                pattern = state.getFieldPattern("code")
            ),
            express = FieldConfig(
                start = state.getFieldStart("express"),
                end = state.getFieldEnd("express"),
                pattern = state.getFieldPattern("express")
            ),
            address = FieldConfig(
                start = state.getFieldStart("address"),
                end = state.getFieldEnd("address"),
                pattern = state.getFieldPattern("address")
            )
        ),
        createTime = "",
        keyword = if (includeKeyword) state.keyword else ""
    )

    /**
     * 拿最近 30 天的真实短信回测当前规则：命中多少条、前几条抽出来长什么样。
     * 比单条预览更能说明「这条规则到底能不能用」。
     */
    fun runBacktest(context: Context) {
        if (_uiState.value.backtestLoading) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "需要短信权限才能回测", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(backtestLoading = true) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val rule = buildRule(_uiState.value, id = "backtest", includeKeyword = true)
                    val messages = smsReader.readRecentSms(30)
                    val samples = mutableListOf<BacktestSample>()
                    var matched = 0
                    for (msg in messages) {
                        val info = matchEngine.extractInfo(msg.content, listOf(rule))
                        if (info.codes.isEmpty()) continue
                        matched++
                        if (samples.size < 5) {
                            samples.add(
                                BacktestSample(
                                    sms = msg.content.replace("\n", " ").take(60),
                                    code = info.codes.joinToString(", "),
                                    express = info.express,
                                    address = info.address
                                )
                            )
                        }
                    }
                    BacktestResult(scanned = messages.size, matched = matched, samples = samples)
                }
            }
            _uiState.update { it.copy(backtestLoading = false, backtest = result.getOrNull()) }
            result.exceptionOrNull()?.let {
                Toast.makeText(context, "回测失败: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun dismissBacktest() {
        _uiState.update { it.copy(backtest = null) }
    }
}
