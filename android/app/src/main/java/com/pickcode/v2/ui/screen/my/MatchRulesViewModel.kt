package com.pickcode.v2.ui.screen.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.repository.MatchRuleRepository
import com.pickcode.v2.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MatchRulesViewModel @Inject constructor(
    private val repository: MatchRuleRepository
) : ViewModel() {

    val rules: StateFlow<List<MatchRule>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _presets = MutableStateFlow(listOf(
        MatchRule(
            id = "preset_tuxi",
            name = "兔喜生活",
            matchType = "startEnd",
            rules = RuleSet(
                code = FieldConfig(start = "凭", end = "来"),
                express = FieldConfig(start = "的", end = "包裹"),
                address = FieldConfig(start = "到", end = "，")
            ),
            createTime = ""
        ),
        MatchRule(
            id = "preset_diguanjia1",
            name = "递管家模板1",
            matchType = "startEnd",
            rules = RuleSet(
                code = FieldConfig(start = "取件码", end = "到"),
                express = FieldConfig(start = "的", end = "已到"),
                address = FieldConfig(start = "已到", end = "，")
            ),
            createTime = ""
        ),
        MatchRule(
            id = "preset_diguanjia2",
            name = "递管家模板2",
            matchType = "startEnd",
            rules = RuleSet(
                code = FieldConfig(start = "取件码", end = "取"),
                express = FieldConfig(start = "的", end = "已到"),
                address = FieldConfig(start = "已到", end = "，")
            ),
            createTime = ""
        )
    ))
    val presets: StateFlow<List<MatchRule>> = _presets.asStateFlow()

    fun toggleEnabled(rule: MatchRule) {
        viewModelScope.launch {
            repository.update(rule.copy(enabled = !rule.enabled))
        }
    }

    fun deleteRule(rule: MatchRule) {
        viewModelScope.launch { repository.delete(rule) }
    }

    fun importPreset(preset: MatchRule) {
        viewModelScope.launch {
            val existing = repository.countByName(preset.name)
            if (existing > 0) return@launch
            repository.insert(
                preset.copy(
                    id = Date().time.toString(),
                    enabled = true,
                    createTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
            )
        }
    }
}
