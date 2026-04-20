package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import com.example.calorietracker.domain.calculator.NutritionCalculator
import com.example.calorietracker.domain.constants.NutritionConstants
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.util.DateUtils
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AnalyticsPeriod {
    DAY,
    WEEK,
    MONTH
}

data class TrendPoint(
    val label: String,
    val calories: Double
)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.DAY,
    val totalCalories: Double = 0.0,
    val targetCalories: Double = 0.0,
    val carbsRatio: Double = 0.0,
    val proteinRatio: Double = 0.0,
    val fatRatio: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val feedback: String = "",
    val trend: List<TrendPoint> = emptyList(),
    val hasData: Boolean = false
)

class AnalyticsViewModel(
    private val repository: CalorieTrackerRepository,
    private val nutritionCalculator: NutritionCalculator
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(AnalyticsPeriod.DAY)
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(250)
            combine(
                selectedPeriod,
                repository.observeCurrentUserProfile(),
                repository.observeCurrentUserRecords()
            ) { period, profile, records ->
                if (profile == null) {
                    AnalyticsUiState(isLoading = false, selectedPeriod = period)
                } else {
                    val target = nutritionCalculator.calculateNutritionTarget(profile)
                    buildAnalyticsState(period, target.tdeeCalories, records)
                }
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun selectPeriod(period: AnalyticsPeriod) {
        selectedPeriod.value = period
    }

    private fun buildAnalyticsState(
        period: AnalyticsPeriod,
        dailyTargetCalories: Double,
        records: List<DietRecord>
    ): AnalyticsUiState {
        val filtered = records.filter { it.isWithin(period) }
        val totalCalories = filtered.sumOf { it.calories }
        val totalCarbs = filtered.sumOf { it.carbs }
        val totalProtein = filtered.sumOf { it.protein }
        val totalFat = filtered.sumOf { it.fat }
        val macroCalories =
            totalCarbs * NutritionConstants.CARB_KCAL_PER_GRAM +
                totalProtein * NutritionConstants.PROTEIN_KCAL_PER_GRAM +
                totalFat * NutritionConstants.FAT_KCAL_PER_GRAM

        val targetCalories = when (period) {
            AnalyticsPeriod.DAY -> dailyTargetCalories
            AnalyticsPeriod.WEEK -> dailyTargetCalories * 7
            AnalyticsPeriod.MONTH -> dailyTargetCalories * DateUtils.daysInCurrentMonth()
        }

        val trend = buildTrend(period, records)
        val ratio = if (targetCalories == 0.0) 0.0 else totalCalories / targetCalories
        val feedback = when {
            totalCalories == 0.0 -> "No intake recorded yet for this period."
            ratio < 0.9 -> "Under target. Add more balanced meals to stay close to your goal."
            ratio > 1.1 -> "Over target. Check portion size and late snacks."
            else -> "Balanced intake. You're staying close to your calorie target."
        }

        return AnalyticsUiState(
            isLoading = false,
            selectedPeriod = period,
            totalCalories = totalCalories,
            targetCalories = targetCalories,
            carbsRatio = if (macroCalories == 0.0) 0.0 else totalCarbs * 4.0 / macroCalories,
            proteinRatio = if (macroCalories == 0.0) 0.0 else totalProtein * 4.0 / macroCalories,
            fatRatio = if (macroCalories == 0.0) 0.0 else totalFat * 9.0 / macroCalories,
            totalCarbs = totalCarbs,
            totalProtein = totalProtein,
            totalFat = totalFat,
            feedback = feedback,
            trend = trend,
            hasData = filtered.isNotEmpty()
        )
    }

    private fun buildTrend(period: AnalyticsPeriod, records: List<DietRecord>): List<TrendPoint> {
        return when (period) {
            AnalyticsPeriod.DAY -> listOf(
                TrendPoint("Today", records.filter { it.isWithin(AnalyticsPeriod.DAY) }.sumOf { it.calories })
            )

            AnalyticsPeriod.WEEK -> (6 downTo 0).map { dayOffset ->
                val time = DateUtils.daysAgo(dayOffset)
                val key = DateUtils.dateKeyFor(time)
                TrendPoint(
                    label = DateUtils.friendlyDate(time),
                    calories = records.filter { it.consumedDate == key }.sumOf { it.calories }
                )
            }

            AnalyticsPeriod.MONTH -> {
                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                val weekBuckets = mutableListOf<TrendPoint>()
                var startDay = 1
                while (startDay <= currentDay) {
                    val endDay = minOf(startDay + 6, currentDay)
                    val calories = records.filter { record ->
                        val recordCalendar = Calendar.getInstance()
                        recordCalendar.timeInMillis = record.consumedAt
                        recordCalendar.get(Calendar.DAY_OF_MONTH) in startDay..endDay &&
                            recordCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                            recordCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                    }.sumOf { it.calories }
                    weekBuckets += TrendPoint("W${weekBuckets.size + 1}", calories)
                    startDay += 7
                }
                weekBuckets
            }
        }
    }

    private fun DietRecord.isWithin(period: AnalyticsPeriod): Boolean {
        val now = System.currentTimeMillis()
        return when (period) {
            AnalyticsPeriod.DAY -> consumedAt >= DateUtils.startOfToday()
            AnalyticsPeriod.WEEK -> consumedAt >= DateUtils.startOfWeek(now)
            AnalyticsPeriod.MONTH -> consumedAt >= DateUtils.startOfMonth(now)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = calorieTrackerApplication().container
                AnalyticsViewModel(
                    repository = container.repository,
                    nutritionCalculator = container.nutritionCalculator
                )
            }
        }
    }
}
