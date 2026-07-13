package com.siamoonga.attendance.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siamoonga.attendance.data.AttendanceRepository
import com.siamoonga.attendance.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentHistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class StudentHistoryViewModel : ViewModel() {

    private val repository = AttendanceRepository()

    private val _uiState = MutableStateFlow(StudentHistoryUiState())
    val uiState: StateFlow<StudentHistoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getMyHistory().fold(
                onSuccess = { list -> _uiState.update { it.copy(isLoading = false, entries = list) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}