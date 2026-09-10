package com.pickcode.v2.ui.screen.pickup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.datastore.SettingsDataStore
import com.pickcode.v2.data.repository.PackageCodeRepository
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

    /** suspend：等写库真正完成再由页面 popBackStack，否则 viewModelScope 会随页面销毁被取消，编辑可能丢。 */
    suspend fun save(): Boolean {
        val state = _uiState.value
        if (state.code.isBlank()) return false
        if (codeId > 0) {
            repository.getById(codeId)?.let { existing ->
                repository.update(
                    existing.copy(
                        code = state.code,
                        company = state.company.ifEmpty { "手动添加" },
                        tags = state.selectedTags,
                        remark = state.remark,
                        isPicked = state.isPicked
                    )
                )
            }
        }
        return true
    }
}
