package com.example.calorietracker.domain.model

data class DailyStats(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val totalCalories: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val carbsRatio: Double = 0.0,
    val proteinRatio: Double = 0.0,
    val fatRatio: Double = 0.0,
    val targetCalories: Double = 0.0,
    val targetCarbs: Double = 0.0,
    val targetProtein: Double = 0.0,
    val targetFat: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)
