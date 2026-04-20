package com.example.calorietracker.domain.model

data class DietRecord(
    val id: String = "",
    val userId: String = "",
    val foodId: String = "",
    val foodName: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val grams: Double = 0.0,
    val consumedAt: Long = System.currentTimeMillis(),
    val consumedDate: String = "",
    val calories: Double = 0.0,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val isCustomFood: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val nutritionValues: NutritionValues
        get() = NutritionValues(
            calories = calories,
            carbs = carbs,
            protein = protein,
            fat = fat
        )
}
