package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import com.example.calorietracker.domain.calculator.NutritionCalculator
import com.example.calorietracker.domain.model.ActivityLevel
import com.example.calorietracker.domain.model.Gender
import com.example.calorietracker.domain.model.NutritionTarget
import com.example.calorietracker.domain.model.UserProfile
import com.example.calorietracker.domain.util.isBodyMetricsValid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val age: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val gender: Gender = Gender.FEMALE,
    val activityLevel: ActivityLevel = ActivityLevel.LIGHT,
    val targetPreview: NutritionTarget? = null,
    val errorMessage: String? = null,
    val savedMessage: String? = null
)

class ProfileViewModel(
    private val repository: CalorieTrackerRepository,
    private val nutritionCalculator: NutritionCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var seededFromProfile = false

    init {
        viewModelScope.launch {
            repository.observeCurrentUserProfile().collect { profile ->
                if (!seededFromProfile) {
                    _uiState.update {
                        val seededState = if (profile == null) {
                            it.copy(isLoading = false)
                        } else {
                            it.copy(
                                isLoading = false,
                                age = profile.age.toString(),
                                heightCm = profile.heightCm.toInt().toString(),
                                weightKg = profile.weightKg.toString(),
                                gender = profile.gender,
                                activityLevel = profile.activityLevel
                            )
                        }
                        seededState.copy(targetPreview = calculatePreview(seededState))
                    }
                    seededFromProfile = true
                }
            }
        }
    }

    fun updateAge(value: String) = updateForm { it.copy(age = value) }

    fun updateHeight(value: String) = updateForm { it.copy(heightCm = value) }

    fun updateWeight(value: String) = updateForm { it.copy(weightKg = value) }

    fun updateGender(value: Gender) = updateForm { it.copy(gender = value) }

    fun updateActivityLevel(value: ActivityLevel) = updateForm { it.copy(activityLevel = value) }

    fun clearSavedMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }

    fun saveProfile() {
        val profile = buildProfileOrNull()
        if (profile == null) {
            _uiState.update { it.copy(errorMessage = "Complete all profile fields with valid numbers.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, savedMessage = null) }
            val result = repository.saveProfile(profile)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message,
                    savedMessage = if (result.isSuccess) "Profile saved successfully." else null
                )
            }
        }
    }

    private fun updateForm(transform: (ProfileUiState) -> ProfileUiState) {
        _uiState.update { current ->
            val updated = transform(current).copy(errorMessage = null, savedMessage = null)
            updated.copy(targetPreview = calculatePreview(updated))
        }
    }

    private fun calculatePreview(state: ProfileUiState): NutritionTarget? {
        val profile = state.toProfileOrNull() ?: return null
        if (!profile.isBodyMetricsValid()) return null
        return nutritionCalculator.calculateNutritionTarget(profile)
    }

    private fun buildProfileOrNull(): UserProfile? = _uiState.value.toProfileOrNull()

    private fun ProfileUiState.toProfileOrNull(): UserProfile? {
        val ageValue = age.toIntOrNull() ?: return null
        val heightValue = heightCm.toDoubleOrNull() ?: return null
        val weightValue = weightKg.toDoubleOrNull() ?: return null
        return UserProfile(
            age = ageValue,
            heightCm = heightValue,
            weightKg = weightValue,
            gender = gender,
            activityLevel = activityLevel
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = calorieTrackerApplication().container
                ProfileViewModel(
                    repository = container.repository,
                    nutritionCalculator = container.nutritionCalculator
                )
            }
        }
    }
}
