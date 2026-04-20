package com.example.calorietracker.domain.constants

import com.example.calorietracker.domain.model.ActivityLevel

object NutritionConstants {
    const val CARB_KCAL_PER_GRAM = 4.0
    const val PROTEIN_KCAL_PER_GRAM = 4.0
    const val FAT_KCAL_PER_GRAM = 9.0

    const val DEFAULT_CARB_RATIO = 0.50
    const val DEFAULT_PROTEIN_RATIO = 0.25
    const val DEFAULT_FAT_RATIO = 0.25

    const val MIN_AGE = 5
    const val MAX_AGE = 100
    const val MIN_HEIGHT_CM = 80.0
    const val MAX_HEIGHT_CM = 250.0
    const val MIN_WEIGHT_KG = 20.0
    const val MAX_WEIGHT_KG = 300.0

    val activityMultipliers: Map<ActivityLevel, Double> = mapOf(
        ActivityLevel.SEDENTARY to 1.2,
        ActivityLevel.LIGHT to 1.375,
        ActivityLevel.MODERATE to 1.55,
        ActivityLevel.HIGH to 1.725
    )
}
