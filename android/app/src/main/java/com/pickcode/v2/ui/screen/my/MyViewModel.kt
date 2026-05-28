package com.pickcode.v2.ui.screen.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickcode.v2.data.repository.PackageCodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: PackageCodeRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(Pair(0, 0))
    val stats: StateFlow<Pair<Int, Int>> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { codes ->
                _stats.value = Pair(codes.size, codes.count { it.isPicked })
            }
        }
    }
}
