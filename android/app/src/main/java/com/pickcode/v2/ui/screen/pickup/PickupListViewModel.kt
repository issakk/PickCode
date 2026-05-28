package com.pickcode.v2.ui.screen.pickup

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.repository.MatchRuleRepository
import com.pickcode.v2.data.repository.PackageCodeRepository
import com.pickcode.v2.domain.engine.MatchEngine
import com.pickcode.v2.domain.engine.SmsReader
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.util.LogBuffer
import com.pickcode.v2.ui.util.formatDateChinese
import com.pickcode.v2.ui.util.todayString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed interface PickupListItem {
    data class DateHeader(val date: String, val pendingCount: Int) : PickupListItem
    data class AddressHeader(val date: String, val address: String) : PickupListItem
    data class Code(val item: PackageCode) : PickupListItem
}

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
        viewModelScope.launch {
            repository.getAll().collect { codes ->
                _uiState.value = PickupListUiState(
                    codes = codes,
                    flatItems = buildFlatItems(codes)
                )
            }
        }
    }

    private fun buildFlatItems(codes: List<PackageCode>): List<PickupListItem> {
        val byDate = codes.groupBy { formatDateChinese(it.date) }
            .toSortedMap(compareByDescending { it })
        val result = mutableListOf<PickupListItem>()
        for ((date, dateCodes) in byDate) {
            val pendingCount = dateCodes.count { !it.isPicked }
            result.add(PickupListItem.DateHeader(date, pendingCount))
            val byAddress = dateCodes.groupBy { it.address.ifEmpty { "未知地址" } }
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
            val existing = repository.findByCodeAndDate(codeText, todayPrefix)
            if (existing != null) {
                return@launch
            }
            val now = todayString()
            repository.insert(
                PackageCode(
                    code = codeText,
                    date = now,
                    sendDate = now,
                    company = "手动添加",
                    address = "手动添加",
                    isManual = true
                )
            )
        }
    }

    fun togglePicked(item: PackageCode) {
        viewModelScope.launch {
            repository.update(item.copy(isPicked = !item.isPicked))
        }
    }

    fun deleteCode(item: PackageCode) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun deleteByDate(dateStr: String) {
        viewModelScope.launch {
            val codes = _uiState.value.codes.filter { formatDateChinese(it.date) == dateStr }
            repository.deleteByIds(codes.map { it.id })
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun autoMatch(context: Context) {
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

                val messages = smsReader.readRecentSms(4)
                LogBuffer.d("PickCode", "读取短信数: ${messages.size}")
                val existingCodes = _uiState.value.codes.map { it.code }.toSet()
                val now = todayString()
                var addedCount = 0

                for (msg in messages) {
                    LogBuffer.d("PickCode", "短信: ${msg.content.take(80)}")
                    val info = matchEngine.extractInfo(msg.content, rules)
                    LogBuffer.d("PickCode", "匹配结果: code=${info.code}, express=${info.express}, address=${info.address}")
                    if (info.code.isNotEmpty() && info.code !in existingCodes) {
                        repository.insert(
                            PackageCode(
                                code = info.code,
                                date = msg.sendDate,
                                sendDate = now,
                                company = info.express.ifEmpty { "未知快递" },
                                address = info.address.ifEmpty { "未知地址" },
                                isManual = false
                            )
                        )
                        addedCount++
                    }
                }

                val toastMsg = if (addedCount > 0) "匹配到 $addedCount 个取件码" else "暂无匹配结果"
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "读取短信失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
