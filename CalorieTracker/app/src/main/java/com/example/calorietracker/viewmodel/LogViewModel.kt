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
    val isSavingCustomFood: Boolean = false,
    val searchQuery: String = "",
    val gramsInput: String = "",
    val selectedMealType: MealType = MealType.BREAKFAST,
    val selectedFoodId: String? = null,
    val isCustomFoodFormVisible: Boolean = false,
    val customFoodName: String = "",
    val customCaloriesInput: String = "",
    val customCarbsInput: String = "",
    val customProteinInput: String = "",
    val customFatInput: String = "",
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
                    updated.copy(filteredFoods = filterFoodsByQuery(updated.searchQuery, foods))
                }
            }
        }
    }

    fun updateSearchQuery(value: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = value,
                filteredFoods = filterFoodsByQuery(value, current.foods),
                errorMessage = null,
                savedMessage = null
            )
        }
    }

    fun updateGramsInput(value: String) {
        _uiState.update { it.copy(gramsInput = value, errorMessage = null, savedMessage = null) }
    }

    fun showCustomFoodForm() {
        _uiState.update {
            it.copy(
                isCustomFoodFormVisible = true,
                errorMessage = null,
                savedMessage = null
            )
        }
    }

    fun hideCustomFoodForm() {
        _uiState.update { it.resetCustomFoodForm(errorMessage = null, savedMessage = null) }
    }

    fun updateCustomFoodName(value: String) {
        _uiState.update { it.copy(customFoodName = value, errorMessage = null, savedMessage = null) }
    }

    fun updateCustomCaloriesInput(value: String) {
        _uiState.update { it.copy(customCaloriesInput = value, errorMessage = null, savedMessage = null) }
    }

    fun updateCustomCarbsInput(value: String) {
        _uiState.update { it.copy(customCarbsInput = value, errorMessage = null, savedMessage = null) }
    }

    fun updateCustomProteinInput(value: String) {
        _uiState.update { it.copy(customProteinInput = value, errorMessage = null, savedMessage = null) }
    }

    fun updateCustomFatInput(value: String) {
        _uiState.update { it.copy(customFatInput = value, errorMessage = null, savedMessage = null) }
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

    fun saveCustomFood() {
        val state = _uiState.value
        val validationError = validateCustomFoodInputs(
            name = state.customFoodName,
            caloriesInput = state.customCaloriesInput,
            carbsInput = state.customCarbsInput,
            proteinInput = state.customProteinInput,
            fatInput = state.customFatInput
        )

        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError, savedMessage = null) }
            return
        }

        val customFood = Food(
            name = state.customFoodName.trim(),
            nameKeywords = buildFoodKeywords(state.customFoodName),
            caloriesPer100g = state.customCaloriesInput.toDouble(),
            carbsPer100g = state.customCarbsInput.toDouble(),
            proteinPer100g = state.customProteinInput.toDouble(),
            fatPer100g = state.customFatInput.toDouble()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCustomFood = true, errorMessage = null, savedMessage = null) }
            val result = repository.createCustomFood(customFood)
            _uiState.update { current ->
                val createdFood = result.getOrNull()
                if (createdFood != null) {
                    current.customFoodSaved(createdFood)
                } else {
                    current.copy(
                        isSavingCustomFood = false,
                        errorMessage = result.exceptionOrNull()?.message,
                        savedMessage = null
                    )
                }
            }
        }
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
                    filteredFoods = filterFoodsByQuery("", it.foods),
                    errorMessage = result.exceptionOrNull()?.message,
                    savedMessage = if (result.isSuccess) "Meal saved to today's log." else null
                )
            }
        }
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

internal fun validateCustomFoodInputs(
    name: String,
    caloriesInput: String,
    carbsInput: String,
    proteinInput: String,
    fatInput: String
): String? {
    val calories = caloriesInput.toDoubleOrNull()
    val carbs = carbsInput.toDoubleOrNull()
    val protein = proteinInput.toDoubleOrNull()
    val fat = fatInput.toDoubleOrNull()

    return when {
        name.isBlank() -> "Enter a food name."
        calories == null || calories <= 0.0 -> "Enter calories per 100 g greater than 0."
        carbs == null || carbs < 0.0 -> "Enter carbs per 100 g as 0 or more."
        protein == null || protein < 0.0 -> "Enter protein per 100 g as 0 or more."
        fat == null || fat < 0.0 -> "Enter fat per 100 g as 0 or more."
        else -> null
    }
}

internal fun buildFoodKeywords(name: String): List<String> {
    val normalized = name.trim().lowercase()
    if (normalized.isBlank()) return emptyList()

    val tokens = normalized
        .split(Regex("[\\s\\-_]+"))
        .filter { it.isNotBlank() }

    return (listOf(normalized) + tokens).distinct()
}

internal fun LogUiState.resetCustomFoodForm(
    errorMessage: String? = this.errorMessage,
    savedMessage: String? = this.savedMessage
): LogUiState {
    return copy(
        isSavingCustomFood = false,
        isCustomFoodFormVisible = false,
        customFoodName = "",
        customCaloriesInput = "",
        customCarbsInput = "",
        customProteinInput = "",
        customFatInput = "",
        errorMessage = errorMessage,
        savedMessage = savedMessage
    )
}

internal fun LogUiState.customFoodSaved(food: Food): LogUiState {
    val updatedFoods = (foods + food).distinctBy { it.id }.sortedBy { it.name.lowercase() }
    return resetCustomFoodForm(
        errorMessage = null,
        savedMessage = "Custom food saved. Enter grams to log it."
    ).copy(
        selectedFoodId = food.id,
        foods = updatedFoods,
        filteredFoods = filterFoodsByQuery("", updatedFoods)
    )
}

internal fun filterFoodsByQuery(query: String, foods: List<Food>): List<Food> {
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
