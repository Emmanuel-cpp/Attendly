package com.siamoonga.attendance.ui.lecturer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.siamoonga.attendance.data.SessionRepository
import com.siamoonga.attendance.model.AttendanceRecord
import com.siamoonga.attendance.model.Session
import com.siamoonga.attendance.util.QrCodeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiveSessionUiState(
    val session: Session? = null,
    val qrBitmap: Bitmap? = null,
    val secondsLeft: Long = QrCodeUtil.SLOT_SECONDS,
    val attendees: List<AttendanceRecord> = emptyList(),
    val isEnding: Boolean = false,
    val ended: Boolean = false,
    val error: String? = null
)

class LiveSessionViewModel : ViewModel() {

    private val repository = SessionRepository()
    private var attendanceListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(LiveSessionUiState())
    val uiState: StateFlow<LiveSessionUiState> = _uiState.asStateFlow()

    fun start(session: Session) {
        if (_uiState.value.session != null) return
        _uiState.update { it.copy(session = session) }

        attendanceListener = repository.listenToAttendance(session.id) { records ->
            _uiState.update { it.copy(attendees = records) }
        }

        viewModelScope.launch {
            while (true) {
                val payload = QrCodeUtil.buildPayload(session.id, session.secret)
                val bitmap = QrCodeUtil.toQrBitmap(payload)
                _uiState.update {
                    it.copy(qrBitmap = bitmap, secondsLeft = QrCodeUtil.secondsLeftInSlot())
                }
                while (QrCodeUtil.secondsLeftInSlot() > 0 &&
                    QrCodeUtil.secondsLeftInSlot() <= QrCodeUtil.SLOT_SECONDS
                ) {
                    _uiState.update { it.copy(secondsLeft = QrCodeUtil.secondsLeftInSlot()) }
                    delay(1000)
                    if (QrCodeUtil.secondsLeftInSlot() == QrCodeUtil.SLOT_SECONDS) break
                }
            }
        }
    }

    fun endSession() {
        val session = _uiState.value.session ?: return
        _uiState.update { it.copy(isEnding = true) }
        viewModelScope.launch {
            repository.endSession(session.id).fold(
                onSuccess = { _uiState.update { it.copy(isEnding = false, ended = true) } },
                onFailure = { e -> _uiState.update { it.copy(isEnding = false, error = e.message) } }
            )
        }
    }

    override fun onCleared() {
        attendanceListener?.remove()
        super.onCleared()
    }
}