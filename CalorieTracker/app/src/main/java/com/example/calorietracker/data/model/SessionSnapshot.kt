package com.example.calorietracker.data.model

data class SessionSnapshot(
    val isLoading: Boolean = true,
    val currentUser: AuthenticatedUser? = null,
    val isProfileComplete: Boolean = false
)
