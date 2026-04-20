package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Food
import com.example.calorietracker.domain.model.MealType
import com.example.calorietracker.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val searchQuery: String = "",
    val gramsInput: String = "",
    val selectedMealType: MealType = MealType.BREAKFAST,
    val selectedFoodId: String? = null,
    val foods: List<Food> = emptyList(),
    val filteredFoods: List<Food> = emptyList(),
    val todayRecords: List<DietRecord> = emptyList(),
    val errorMessage: String? = null,
    val savedMessage: String? = null
) {
    val selectedFood: Food?
        get() = foods.firstOrNull { it.id == selectedFoodId }
}

class LogViewModel(
    private val repository: CalorieTrackerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(250)
            combine(
                repository.observeFoods(),
                repository.observeCurrentUserRecords()
            ) { foods, records ->
                foods to records.filter { it.consumedDate == DateUtils.todayKey() }
            }.collect { (foods, records) ->
                _uiState.update { current ->
                    val updated = current.copy(
                        isLoading = false,
                        foods = foods,
                        todayRecords = records.sortedByDescending { it.consumedAt }
                    )
                    updated.copy(filteredFoods = filterFoods(updated.searchQuery, foods))
                }
            }
        }
    }

    fun updateSearchQuery(value: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = value,
                filteredFoods = filterFoods(value, current.foods),
                errorMessage = null,
                savedMessage = null
            )
        }
    }

    fun updateGramsInput(value: String) {
        _uiState.update { it.copy(gramsInput = value, errorMessage = null, savedMessage = null) }
    }

    fun selectMealType(value: MealType) {
        _uiState.update { it.copy(selectedMealType = value, errorMessage = null) }
    }

    fun selectFood(foodId: String) {
        _uiState.update { it.copy(selectedFoodId = foodId, errorMessage = null, savedMessage = null) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(errorMessage = null, savedMessage = null) }
    }

    fun saveRecord() {
        val state = _uiState.value
        val selectedFood = state.selectedFood
        val grams = state.gramsInput.toDoubleOrNull()

        val validationError = when {
            selectedFood == null -> "Select a food from the search results first."
            grams == null || grams <= 0.0 -> "Enter a valid gram amount."
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError, savedMessage = null) }
            return
        }

        val safeFood = selectedFood ?: return
        val safeGrams = grams ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, savedMessage = null) }
            val result = repository.addDietRecord(
                food = safeFood,
                grams = safeGrams,
                mealType = state.selectedMealType
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    searchQuery = "",
                    gramsInput = "",
                    selectedFoodId = null,
                    filteredFoods = filterFoods("", it.foods),
                    errorMessage = result.exceptionOrNull()?.message,
                    savedMessage = if (result.isSuccess) "Meal saved to today's log." else null
                )
            }
        }
    }

    private fun filterFoods(query: String, foods: List<Food>): List<Food> {
        if (query.isBlank()) return foods.take(6)
        val normalizedQuery = query.trim().lowercase()
        return foods
            .map { food ->
                val nameScore = when {
                    food.name.lowercase() == normalizedQuery -> 3
                    food.name.lowercase().contains(normalizedQuery) -> 2
                    food.nameKeywords.any { it.contains(normalizedQuery, ignoreCase = true) } -> 1
                    else -> 0
                }
                food to nameScore
            }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<Food, Int>> { it.second }.thenBy { it.first.name })
            .map { it.first }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                LogViewModel(
                    repository = calorieTrackerApplication().container.repository
                )
            }
        }
    }
}
