package com.example.calorietracker.domain.calculator

import com.example.calorietracker.domain.model.DailyStats
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Food
import com.example.calorietracker.domain.model.NutritionTarget
import com.example.calorietracker.domain.model.NutritionValues
import com.example.calorietracker.domain.model.UserProfile

interface NutritionCalculator {
    fun calculateFoodNutrition(food: Food, grams: Double): NutritionValues
    fun calculateNutritionTarget(profile: UserProfile): NutritionTarget
    fun aggregateDailyStats(
        userId: String,
        date: String,
        records: List<DietRecord>,
        target: NutritionTarget
    ): DailyStats
}
