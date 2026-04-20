package com.example.calorietracker.domain.util

import com.example.calorietracker.domain.constants.NutritionConstants
import com.example.calorietracker.domain.model.UserProfile

fun UserProfile.isBodyMetricsValid(): Boolean {
    return age in NutritionConstants.MIN_AGE..NutritionConstants.MAX_AGE &&
        heightCm in NutritionConstants.MIN_HEIGHT_CM..NutritionConstants.MAX_HEIGHT_CM &&
        weightKg in NutritionConstants.MIN_WEIGHT_KG..NutritionConstants.MAX_WEIGHT_KG
}
