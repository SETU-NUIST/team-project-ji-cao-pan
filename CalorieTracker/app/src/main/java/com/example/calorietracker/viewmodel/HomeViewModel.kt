package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import com.example.calorietracker.domain.calculator.NutritionCalculator
import com.example.calorietracker.domain.model.DailyStats
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.NutritionTarget
import com.example.calorietracker.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val target: NutritionTarget? = null,
    val dailyStats: DailyStats? = null,
    val todayRecords: List<DietRecord> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: CalorieTrackerRepository,
    private val nutritionCalculator: NutritionCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(250)
            combine(
                repository.observeCurrentUserProfile(),
                repository.observeCurrentUserRecords()
            ) { profile, records ->
                if (profile == null) {
                    HomeUiState(isLoading = false, errorMessage = "Complete your profile to see the dashboard.")
                } else {
                    val target = nutritionCalculator.calculateNutritionTarget(profile)
                    val todayRecords = records.filter { it.consumedDate == DateUtils.todayKey() }
                    val stats = nutritionCalculator.aggregateDailyStats(
                        userId = profile.id,
                        date = DateUtils.todayKey(),
                        records = todayRecords,
                        target = target
                    )
                    HomeUiState(
                        isLoading = false,
                        target = target,
                        dailyStats = stats,
                        todayRecords = todayRecords
                    )
                }
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = calorieTrackerApplication().container
                HomeViewModel(
                    repository = container.repository,
                    nutritionCalculator = container.nutritionCalculator
                )
            }
        }
    }
}
