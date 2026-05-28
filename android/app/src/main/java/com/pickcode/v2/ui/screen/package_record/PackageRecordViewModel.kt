package com.pickcode.v2.ui.screen.package_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.repository.PackageRecordRepository
import com.pickcode.v2.domain.model.PackageRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackageRecordUiState(
    val records: List<PackageRecord> = emptyList(),
    val groupedRecords: Map<String, List<PackageRecord>> = emptyMap()
)

@HiltViewModel
class PackageRecordViewModel @Inject constructor(
    private val repository: PackageRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageRecordUiState())
    val uiState: StateFlow<PackageRecordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { records ->
                val grouped = records.groupBy { it.date }.toSortedMap(compareByDescending { it })
                _uiState.value = PackageRecordUiState(records, grouped)
            }
        }
    }

    fun toggleChecked(record: PackageRecord) {
        viewModelScope.launch {
            repository.update(record.copy(checked = !record.checked))
        }
    }

    fun deleteRecord(record: PackageRecord) {
        viewModelScope.launch { repository.delete(record) }
    }

    fun deleteByDate(date: String) {
        viewModelScope.launch {
            val toDelete = _uiState.value.records.filter { it.date == date }
            toDelete.forEach { repository.delete(it) }
        }
    }
}
