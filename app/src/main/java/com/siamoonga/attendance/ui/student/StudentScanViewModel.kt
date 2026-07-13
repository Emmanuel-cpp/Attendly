package com.siamoonga.attendance.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siamoonga.attendance.data.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ScanStatus {
    data object Scanning : ScanStatus()
    data object Processing : ScanStatus()
    data class Success(val courseCode: String) : ScanStatus()
    data class Failed(val message: String) : ScanStatus()
}

data class StudentScanUiState(
    val status: ScanStatus = ScanStatus.Scanning
)

class StudentScanViewModel : ViewModel() {

    private val repository = AttendanceRepository()

    private val _uiState = MutableStateFlow(StudentScanUiState())
    val uiState: StateFlow<StudentScanUiState> = _uiState.asStateFlow()

    fun onQrDetected(payload: String) {
        if (_uiState.value.status != ScanStatus.Scanning) return
        _uiState.update { it.copy(status = ScanStatus.Processing) }

        viewModelScope.launch {
            repository.markAttendance(payload).fold(
                onSuccess = { courseCode ->
                    _uiState.update { it.copy(status = ScanStatus.Success(courseCode)) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(status = ScanStatus.Failed(e.message ?: "Scan failed")) }
                }
            )
        }
    }

    fun scanAgain() {
        _uiState.update { it.copy(status = ScanStatus.Scanning) }
    }
}