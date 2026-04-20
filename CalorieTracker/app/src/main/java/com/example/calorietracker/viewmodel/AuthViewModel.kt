package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calorietracker.data.firebase.toAuthErrorMessage
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: CalorieTrackerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun switchMode(mode: AuthMode) {
        _uiState.update {
            it.copy(mode = mode, errorMessage = null)
        }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        val validationError = when (state.mode) {
            AuthMode.LOGIN -> validateLogin(state)
            AuthMode.REGISTER -> validateRegistration(state)
        }

        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = when (state.mode) {
                AuthMode.LOGIN -> repository.signIn(state.email, state.password)
                AuthMode.REGISTER -> repository.register(state.username, state.email, state.password)
            }
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.toAuthErrorMessage(
                        isRegistration = state.mode == AuthMode.REGISTER
                    )
                )
            }
        }
    }

    private fun validateLogin(state: AuthUiState): String? {
        if (!state.email.contains("@")) return "Enter a valid email address."
        if (state.password.length < 6) return "Password must be at least 6 characters."
        return null
    }

    private fun validateRegistration(state: AuthUiState): String? {
        if (state.username.trim().length < 2) return "Enter a username with at least 2 characters."
        if (!state.email.contains("@")) return "Enter a valid email address."
        if (state.password.length < 6) return "Password must be at least 6 characters."
        if (state.password != state.confirmPassword) return "Passwords do not match."
        return null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                AuthViewModel(
                    repository = calorieTrackerApplication().container.repository
                )
            }
        }
    }
}
