package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.SessionSnapshot
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Food
import com.example.calorietracker.domain.model.MealType
import com.example.calorietracker.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CalorieTrackerRepository {
    val sessionState: StateFlow<SessionSnapshot>

    fun observeCurrentUserProfile(): Flow<UserProfile?>
    fun observeFoods(): Flow<List<Food>>
    fun observeCurrentUserRecords(): Flow<List<DietRecord>>

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun register(username: String, email: String, password: String): Result<Unit>
    suspend fun signOut()
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
    suspend fun createCustomFood(food: Food): Result<Food>
    suspend fun addDietRecord(food: Food, grams: Double, mealType: MealType): Result<DietRecord>
    suspend fun getRecord(recordId: String): DietRecord?
}
