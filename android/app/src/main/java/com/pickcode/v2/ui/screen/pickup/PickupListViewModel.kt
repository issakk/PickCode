package com.pickcode.v2.ui.screen.pickup

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.repository.PackageCodeRepository
import com.pickcode.v2.domain.engine.CodeImporter
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.util.formatDateChinese
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Immutable
sealed interface PickupListItem {
    @Immutable
    data class DateHeader(val date: String, val pendingCount: Int) : PickupListItem
    @Immutable
    data class AddressHeader(val date: String, val address: String) : PickupListItem
    @Immutable
    data class Code(val item: PackageCode) : PickupListItem
}

@Stable
data class PickupListUiState(
    val codes: List<PackageCode> = emptyList(),
    val flatItems: List<PickupListItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class PickupListViewModel @Inject constructor(
    private val repository: PackageCodeRepository,
    private val codeImporter: CodeImporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(PickupListUiState())
    val uiState: StateFlow<PickupListUiState> = _uiState.asStateFlow()

    init {
        reload()
    }

    /** 重新从数据库读取。编辑页返回（onResume）时调用，否则列表会停留在旧数据。 */
    fun reload() {
        viewModelScope.launch { refresh() }
    }

    private suspend fun refresh() {
        val codes = withContext(Dispatchers.IO) { repository.getAllOnce() }
        _uiState.value = PickupListUiState(
            codes = codes,
            flatItems = buildFlatItems(codes)
        )
    }

    private fun buildFlatItems(codes: List<PackageCode>): List<PickupListItem> {
        val byDate = linkedMapOf<String, MutableList<PackageCode>>()
        for (code in codes) {
            val key = formatDateChinese(code.date)
            byDate.getOrPut(key) { mutableListOf() }.add(code)
        }
        val result = mutableListOf<PickupListItem>()
        for ((date, dateCodes) in byDate) {
            val pendingCount = dateCodes.count { !it.isPicked }
            result.add(PickupListItem.DateHeader(date, pendingCount))
            val byAddress = linkedMapOf<String, MutableList<PackageCode>>()
            for (code in dateCodes) {
                val addr = code.address.ifEmpty { "未知地址" }
                byAddress.getOrPut(addr) { mutableListOf() }.add(code)
            }
            for ((address, addressCodes) in byAddress) {
                result.add(PickupListItem.AddressHeader(date, address))
                for (code in addressCodes) {
                    result.add(PickupListItem.Code(code))
                }
            }
        }
        return result
    }

    fun togglePicked(item: PackageCode) {
        val toggled = item.copy(isPicked = !item.isPicked)
        _uiState.update { current ->
            val updatedCodes = current.codes.map { if (it.id == item.id) toggled else it }
            current.copy(codes = updatedCodes, flatItems = buildFlatItems(updatedCodes))
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(toggled)
        }
    }

    fun deleteCode(item: PackageCode) {
        _uiState.update { current ->
            val updatedCodes = current.codes.filter { it.id != item.id }
            current.copy(codes = updatedCodes, flatItems = buildFlatItems(updatedCodes))
        }
        viewModelScope.launch(Dispatchers.IO) { repository.delete(item) }
    }

    fun deleteByDate(dateStr: String) {
        val idsToDelete = _uiState.value.codes.filter { formatDateChinese(it.date) == dateStr }.map { it.id }
        _uiState.update { current ->
            val updatedCodes = current.codes.filter { formatDateChinese(it.date) != dateStr }
            current.copy(codes = updatedCodes, flatItems = buildFlatItems(updatedCodes))
        }
        viewModelScope.launch(Dispatchers.IO) { repository.deleteByIds(idsToDelete) }
    }

    fun deleteAll() {
        _uiState.update { it.copy(codes = emptyList(), flatItems = emptyList()) }
        viewModelScope.launch(Dispatchers.IO) { repository.deleteAll() }
    }

    fun autoMatch(context: Context, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 读短信 + 跑正则 + 入库全部丢到 IO 线程，避免正则回溯卡住主线程
            val result = withContext(Dispatchers.IO) {
                runCatching { codeImporter.importFromSms(4) }
            }
            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { outcome ->
                    when (outcome) {
                        is CodeImporter.Outcome.NoRules ->
                            Toast.makeText(context, "请先添加匹配规则", Toast.LENGTH_SHORT).show()

                        is CodeImporter.Outcome.Done -> {
                            refresh()
                            Toast.makeText(
                                context,
                                if (outcome.addedCount > 0) "匹配到 ${outcome.addedCount} 个取件码" else "暂无匹配结果",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (outcome.addedCount > 0) onSuccess()
                        }
                    }
                },
                onFailure = { e ->
                    Toast.makeText(context, "读取短信失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
