package com.example.calorietracker

import com.example.calorietracker.domain.calculator.DefaultNutritionCalculator
import com.example.calorietracker.domain.model.ActivityLevel
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Gender
import com.example.calorietracker.domain.model.MealType
import com.example.calorietracker.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private val calculator = DefaultNutritionCalculator()

    @Test
    fun calculateNutritionTarget_usesProfileInputs() {
        val profile = UserProfile(
            gender = Gender.FEMALE,
            age = 24,
            heightCm = 165.0,
            weightKg = 58.0,
            activityLevel = ActivityLevel.LIGHT
        )

        val target = calculator.calculateNutritionTarget(profile)

        assertTrue(target.tdeeCalories > 1800.0)
        assertEquals(0.5, target.carbsRatio, 0.0)
        assertEquals(0.25, target.proteinRatio, 0.0)
        assertEquals(0.25, target.fatRatio, 0.0)
    }

    @Test
    fun aggregateDailyStats_sumsSnapshotsAndRatios() {
        val target = calculator.calculateNutritionTarget(
            UserProfile(
                gender = Gender.MALE,
                age = 22,
                heightCm = 178.0,
                weightKg = 72.0,
                activityLevel = ActivityLevel.MODERATE
            )
        )

        val records = listOf(
            DietRecord(
                id = "1",
                userId = "user",
                foodName = "Avocado Toast",
                mealType = MealType.BREAKFAST,
                grams = 150.0,
                calories = 240.0,
                carbs = 27.0,
                protein = 7.5,
                fat = 12.0
            ),
            DietRecord(
                id = "2",
                userId = "user",
                foodName = "Greek Yogurt",
                mealType = MealType.SNACK,
                grams = 180.0,
                calories = 174.6,
                carbs = 7.0,
                protein = 18.0,
                fat = 9.0
            )
        )

        val stats = calculator.aggregateDailyStats(
            userId = "user",
            date = "2026-04-19",
            records = records,
            target = target
        )

        assertEquals(414.6, stats.totalCalories, 0.01)
        assertEquals(34.0, stats.totalCarbs, 0.01)
        assertEquals(25.5, stats.totalProtein, 0.01)
        assertEquals(21.0, stats.totalFat, 0.01)
        assertTrue(stats.carbsRatio > 0.3)
    }
}
