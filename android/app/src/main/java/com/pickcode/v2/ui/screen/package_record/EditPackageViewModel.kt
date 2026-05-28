package com.pickcode.v2.ui.screen.package_record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.repository.PackageRecordRepository
import com.pickcode.v2.domain.model.PackageRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class EditPackageUiState(
    val platform: String = "淘宝",
    val name: String = "",
    val price: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isEdit: Boolean = false
)

@HiltViewModel
class EditPackageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PackageRecordRepository
) : ViewModel() {

    private val packageId: Long = savedStateHandle.get<Long>("packageId") ?: -1L
    private val type: String = savedStateHandle.get<String>("type") ?: "add"
    private val _uiState = MutableStateFlow(EditPackageUiState(isEdit = type == "edit"))
    val uiState: StateFlow<EditPackageUiState> = _uiState.asStateFlow()

    init {
        if (type == "edit" && packageId > 0) {
            viewModelScope.launch {
                repository.getById(packageId)?.let { record ->
                    _uiState.value = EditPackageUiState(
                        platform = record.platform,
                        name = record.name,
                        price = record.price,
                        date = record.date,
                        isEdit = true
                    )
                }
            }
        }
    }

    fun updatePlatform(v: String) { _uiState.update { it.copy(platform = v) } }
    fun updateName(v: String) { _uiState.update { it.copy(name = v) } }
    fun updatePrice(v: String) { _uiState.update { it.copy(price = v) } }
    fun updateDate(v: String) { _uiState.update { it.copy(date = v) } }

    fun save(): Boolean {
        val state = _uiState.value
        if (state.name.isBlank()) return false
        viewModelScope.launch {
            val record = PackageRecord(
                id = if (state.isEdit) packageId else 0,
                platform = state.platform,
                name = state.name,
                price = state.price.ifEmpty { "0.00" },
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                date = state.date
            )
            if (state.isEdit) repository.update(record) else repository.insert(record)
        }
        return true
    }
}
