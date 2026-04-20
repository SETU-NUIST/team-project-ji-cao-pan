package com.example.calorietracker.domain.model

data class Food(
    val id: String = "",
    val name: String = "",
    val nameKeywords: List<String> = emptyList(),
    val caloriesPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val isBaseFood: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun nutritionFor(grams: Double): NutritionValues {
        val factor = grams / HUNDRED_GRAMS
        return NutritionValues(
            calories = caloriesPer100g * factor,
            carbs = carbsPer100g * factor,
            protein = proteinPer100g * factor,
            fat = fatPer100g * factor
        )
    }

    companion object {
        private const val HUNDRED_GRAMS = 100.0
    }
}
