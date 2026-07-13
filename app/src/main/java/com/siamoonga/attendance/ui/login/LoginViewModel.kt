package com.siamoonga.attendance.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siamoonga.attendance.data.AuthRepository
import com.siamoonga.attendance.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInRole: Role? = null
)

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun signIn() {
        val current = _uiState.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(error = "Enter your email and password") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.signIn(current.email, current.password).fold(
                onSuccess = { role -> _uiState.update { it.copy(isLoading = false, loggedInRole = role) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Sign-in failed") } }
            )
        }
    }

    fun onNavigationHandled() = _uiState.update { it.copy(loggedInRole = null) }
}