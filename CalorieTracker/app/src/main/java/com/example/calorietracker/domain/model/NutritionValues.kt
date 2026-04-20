package com.example.calorietracker.domain.model

data class NutritionValues(
    val calories: Double = 0.0,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0
) {
    operator fun plus(other: NutritionValues): NutritionValues {
        return NutritionValues(
            calories = calories + other.calories,
            carbs = carbs + other.carbs,
            protein = protein + other.protein,
            fat = fat + other.fat
        )
    }
}
