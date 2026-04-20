package com.example.calorietracker.domain.model

data class UserProfile(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val gender: Gender = Gender.MALE,
    val age: Int = 18,
    val heightCm: Double = 170.0,
    val weightKg: Double = 65.0,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
