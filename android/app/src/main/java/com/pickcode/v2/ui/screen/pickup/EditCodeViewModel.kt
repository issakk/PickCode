package com.pickcode.v2.ui.screen.pickup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.datastore.SettingsDataStore
import com.pickcode.v2.data.repository.PackageCodeRepository
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.util.todayString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditCodeUiState(
    val code: String = "",
    val company: String = "",
    val selectedTags: List<String> = emptyList(),
    val tagOptions: List<String> = listOf("家", "公司", "老家", "生鲜", "私密"),
    val customTag: String = "",
    val remark: String = "",
    val isPicked: Boolean = false,
    val isLoaded: Boolean = false
)

@HiltViewModel
class EditCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PackageCodeRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val codeId: Long = savedStateHandle.get<Long>("codeId") ?: -1L

    /** codeId <= 0 表示手动新增 */
    val isNew: Boolean = codeId <= 0
    private val _uiState = MutableStateFlow(EditCodeUiState())
    val uiState: StateFlow<EditCodeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.tagOptions.collect { tags ->
                _uiState.update { it.copy(tagOptions = tags) }
            }
        }
        if (codeId > 0) {
            viewModelScope.launch {
                repository.getById(codeId)?.let { code ->
                    _uiState.update {
                        it.copy(
                            code = code.code,
                            company = code.company,
                            selectedTags = code.tags,
                            remark = code.remark,
                            isPicked = code.isPicked,
                            isLoaded = true
                        )
                    }
                }
            }
        }
    }

    fun updateCode(value: String) { _uiState.update { it.copy(code = value) } }
    fun updateCompany(value: String) { _uiState.update { it.copy(company = value) } }
    fun updateRemark(value: String) { _uiState.update { it.copy(remark = value) } }
    fun updateCustomTag(value: String) { _uiState.update { it.copy(customTag = value) } }
    fun updatePicked(value: Boolean) { _uiState.update { it.copy(isPicked = value) } }

    fun toggleTag(tag: String) {
        _uiState.update {
            val tags = if (tag in it.selectedTags) it.selectedTags - tag else it.selectedTags + tag
            it.copy(selectedTags = tags)
        }
    }

    fun deleteTag(tag: String) {
        _uiState.update {
            val newOptions = it.tagOptions - tag
            viewModelScope.launch { settingsDataStore.saveTagOptions(newOptions) }
            it.copy(tagOptions = newOptions, selectedTags = it.selectedTags - tag)
        }
    }

    fun addCustomTag() {
        val tag = _uiState.value.customTag.trim()
        if (tag.isEmpty() || tag in _uiState.value.tagOptions) return
        if (_uiState.value.tagOptions.size >= 10) return
        _uiState.update {
            val newOptions = it.tagOptions + tag
            viewModelScope.launch { settingsDataStore.saveTagOptions(newOptions) }
            it.copy(tagOptions = newOptions, selectedTags = it.selectedTags + tag, customTag = "")
        }
    }

    /**
     * suspend：等写库真正完成再由页面 popBackStack，否则 viewModelScope 会随页面销毁被取消，编辑可能丢。
     * 返回 null 表示成功，否则是要弹给用户的错误文案。
     */
    suspend fun save(): String? {
        val state = _uiState.value
        val code = state.code.trim()
        if (code.isEmpty()) return "请输入取件码"

        if (isNew) {
            val now = todayString()
            val id = repository.insert(
                PackageCode(
                    code = code,
                    date = now,
                    sendDate = now,
                    company = state.company.ifEmpty { "手动添加" },
                    address = "手动添加",
                    isManual = true,
                    tags = state.selectedTags,
                    remark = state.remark,
                    isPicked = state.isPicked
                )
            )
            // insert 返回 -1 说明今天已有同一个取件码
            return if (id == -1L) "今天已经有这个取件码了" else null
        }

        repository.getById(codeId)?.let { existing ->
            repository.update(
                existing.copy(
                    code = code,
                    company = state.company.ifEmpty { "手动添加" },
                    tags = state.selectedTags,
                    remark = state.remark,
                    isPicked = state.isPicked
                )
            )
        }
        return null
    }
}
