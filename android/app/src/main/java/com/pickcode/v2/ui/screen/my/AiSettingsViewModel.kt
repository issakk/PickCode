package com.pickcode.v2.ui.screen.my

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import javax.inject.Inject

data class AiSettingsUiState(
    val endpoint: String = "",
    val apiKey: String = "",
    val model: String = "",
    val isTesting: Boolean = false
)

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val httpClient: HttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsDataStore.aiEndpoint,
                settingsDataStore.aiApiKey,
                settingsDataStore.aiModel
            ) { ep, key, model ->
                AiSettingsUiState(endpoint = ep, apiKey = key, model = model)
            }.collect { _uiState.value = it }
        }
    }

    fun updateEndpoint(v: String) { _uiState.update { it.copy(endpoint = v) } }
    fun updateApiKey(v: String) { _uiState.update { it.copy(apiKey = v) } }
    fun updateModel(v: String) { _uiState.update { it.copy(model = v) } }

    fun save(): Boolean {
        val state = _uiState.value
        if (state.endpoint.isBlank() || state.apiKey.isBlank() || state.model.isBlank()) return false
        viewModelScope.launch {
            settingsDataStore.saveAiConfig(state.endpoint.trimEnd('/'), state.apiKey, state.model)
        }
        return true
    }

    fun testConnection(context: Context) {
        val state = _uiState.value
        if (state.endpoint.isBlank() || state.apiKey.isBlank() || state.model.isBlank()) {
            Toast.makeText(context, "请先填写所有字段", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true) }
            try {
                val url = "${state.endpoint.trimEnd('/')}/v1/chat/completions"
                val response = httpClient.post(url) {
                    header("Authorization", "Bearer ${state.apiKey}")
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("model", state.model)
                        putJsonArray("messages") {
                            addJsonObject {
                                put("role", "user")
                                put("content", "回复OK")
                            }
                        }
                        put("max_tokens", 10)
                    }.toString())
                }
                if (response.status.isSuccess()) {
                    Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "连接失败: ${response.status}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "连接失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _uiState.update { it.copy(isTesting = false) }
            }
        }
    }
}
