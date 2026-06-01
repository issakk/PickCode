package com.pickcode.v2.ui.screen.pickup

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.repository.MatchRuleRepository
import com.pickcode.v2.data.repository.PackageCodeRepository
import com.pickcode.v2.domain.engine.MatchEngine
import com.pickcode.v2.domain.engine.SmsReader
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.util.LogBuffer
import com.pickcode.v2.ui.util.formatDateChinese
import com.pickcode.v2.ui.util.todayString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
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
    private val matchRuleRepository: MatchRuleRepository,
    private val matchEngine: MatchEngine,
    private val smsReader: SmsReader
) : ViewModel() {

    private val _uiState = MutableStateFlow(PickupListUiState())
    val uiState: StateFlow<PickupListUiState> = _uiState.asStateFlow()

    init {
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

    fun addCode(codeText: String) {
        viewModelScope.launch {
            val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existing = withContext(Dispatchers.IO) { repository.findByCodeAndDate(codeText, todayPrefix) }
            if (existing != null) return@launch
            val now = todayString()
            val newCode = PackageCode(
                code = codeText,
                date = now,
                sendDate = now,
                company = "手动添加",
                address = "手动添加",
                isManual = true
            )
            val id = withContext(Dispatchers.IO) { repository.insert(newCode) }
            val savedCode = newCode.copy(id = id)
            _uiState.update { current ->
                val updatedCodes = listOf(savedCode) + current.codes
                current.copy(codes = updatedCodes, flatItems = buildFlatItems(updatedCodes))
            }
        }
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
            try {
                val rules = matchRuleRepository.getEnabled()
                LogBuffer.d("PickCode", "启用规则数: ${rules.size}")
                if (rules.isEmpty()) {
                    Toast.makeText(context, "请先添加匹配规则", Toast.LENGTH_SHORT).show()
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                for (r in rules) {
                    LogBuffer.d("PickCode", "规则: ${r.name}, type=${r.matchType}, code.start=${r.rules.code.start}, code.end=${r.rules.code.end}, code.pattern=${r.rules.code.pattern}")
                }

                val messages = withContext(Dispatchers.IO) { smsReader.readRecentSms(4) }
                LogBuffer.d("PickCode", "读取短信数: ${messages.size}")
                val existingCodes = _uiState.value.codes.map { it.code }.toMutableSet()
                val now = todayString()
                var addedCount = 0

                for (msg in messages) {
                    LogBuffer.d("PickCode", "短信: ${msg.content.take(80)}")
                    val info = matchEngine.extractInfo(msg.content, rules)
                    LogBuffer.d("PickCode", "匹配结果: codes=${info.codes}, express=${info.express}, address=${info.address}")
                    for (code in info.codes) {
                        if (code.isNotEmpty() && code !in existingCodes) {
                            repository.insert(
                                PackageCode(
                                    code = code,
                                    date = msg.sendDate,
                                    sendDate = now,
                                    company = info.express.ifEmpty { "未知快递" },
                                    address = info.address.ifEmpty { "未知地址" },
                                    isManual = false
                                )
                            )
                            existingCodes.add(code)
                            addedCount++
                        }
                    }
                }

                refresh()
                val toastMsg = if (addedCount > 0) "匹配到 $addedCount 个取件码" else "暂无匹配结果"
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                if (addedCount > 0) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "读取短信失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
