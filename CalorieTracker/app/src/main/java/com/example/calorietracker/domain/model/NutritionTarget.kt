package com.example.calorietracker.domain.model

data class NutritionTarget(
    val tdeeCalories: Double = 0.0,
    val targetCarbsGrams: Double = 0.0,
    val targetProteinGrams: Double = 0.0,
    val targetFatGrams: Double = 0.0,
    val carbsRatio: Double = 0.0,
    val proteinRatio: Double = 0.0,
    val fatRatio: Double = 0.0
)
