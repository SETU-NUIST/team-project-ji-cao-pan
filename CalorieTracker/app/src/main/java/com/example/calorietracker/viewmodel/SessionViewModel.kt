package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calorietracker.data.model.AuthenticatedUser
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiState(
    val isLoading: Boolean = true,
    val currentUser: AuthenticatedUser? = null,
    val requiresProfileSetup: Boolean = false
)

class SessionViewModel(
    private val repository: CalorieTrackerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.sessionState.collect { snapshot ->
                _uiState.update {
                    SessionUiState(
                        isLoading = snapshot.isLoading,
                        currentUser = snapshot.currentUser,
                        requiresProfileSetup = snapshot.currentUser != null && !snapshot.isProfileComplete
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SessionViewModel(
                    repository = calorieTrackerApplication().container.repository
                )
            }
        }
    }
}
