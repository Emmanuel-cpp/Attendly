package com.siamoonga.attendance.ui.lecturer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siamoonga.attendance.data.CourseRepository
import com.siamoonga.attendance.data.SessionRepository
import com.siamoonga.attendance.model.Course
import com.siamoonga.attendance.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LecturerCoursesUiState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val newName: String = "",
    val newCode: String = "",
    val isSaving: Boolean = false,
    val startingCourseId: String? = null
)

class LecturerCoursesViewModel : ViewModel() {

    private val repository = CourseRepository()
    private val sessionRepository = SessionRepository()

    private val _uiState = MutableStateFlow(LecturerCoursesUiState())
    val uiState: StateFlow<LecturerCoursesUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getMyCourses().fold(
                onSuccess = { list -> _uiState.update { it.copy(isLoading = false, courses = list) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun onAddClicked() = _uiState.update { it.copy(showAddDialog = true, newName = "", newCode = "") }
    fun onDialogDismiss() = _uiState.update { it.copy(showAddDialog = false) }
    fun onNameChange(v: String) = _uiState.update { it.copy(newName = v) }
    fun onCodeChange(v: String) = _uiState.update { it.copy(newCode = v) }

    fun saveCourse() {
        val state = _uiState.value
        if (state.newName.isBlank() || state.newCode.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.createCourse(state.newName, state.newCode).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, showAddDialog = false) }
                    loadCourses()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, showAddDialog = false, error = e.message) }
                }
            )
        }
    }

    fun startSession(course: Course, onStarted: (Session) -> Unit) {
        _uiState.update { it.copy(startingCourseId = course.id) }
        viewModelScope.launch {
            sessionRepository.startSession(course).fold(
                onSuccess = { session ->
                    _uiState.update { it.copy(startingCourseId = null) }
                    onStarted(session)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(startingCourseId = null, error = e.message) }
                }
            )
        }
    }
}