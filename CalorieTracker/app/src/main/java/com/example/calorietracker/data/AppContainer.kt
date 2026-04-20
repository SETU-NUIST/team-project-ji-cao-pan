package com.example.calorietracker.data

import android.content.Context
import com.example.calorietracker.data.repository.CloudBackedCalorieTrackerRepository
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import com.example.calorietracker.domain.calculator.DefaultNutritionCalculator
import com.example.calorietracker.domain.calculator.NutritionCalculator

interface AppContainer {
    val repository: CalorieTrackerRepository
    val nutritionCalculator: NutritionCalculator
}

class DefaultAppContainer(
    context: Context
) : AppContainer {
    override val nutritionCalculator: NutritionCalculator = DefaultNutritionCalculator()
    override val repository: CalorieTrackerRepository by lazy {
        CloudBackedCalorieTrackerRepository(
            calculator = nutritionCalculator
        )
    }
}
