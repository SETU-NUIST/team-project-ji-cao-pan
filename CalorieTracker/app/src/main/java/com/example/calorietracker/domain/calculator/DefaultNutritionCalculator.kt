package com.example.calorietracker.domain.calculator

import com.example.calorietracker.domain.constants.NutritionConstants
import com.example.calorietracker.domain.model.DailyStats
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Food
import com.example.calorietracker.domain.model.Gender
import com.example.calorietracker.domain.model.NutritionTarget
import com.example.calorietracker.domain.model.NutritionValues
import com.example.calorietracker.domain.model.UserProfile
import com.example.calorietracker.domain.util.isBodyMetricsValid
import kotlin.math.roundToInt

class DefaultNutritionCalculator : NutritionCalculator {

    override fun calculateFoodNutrition(food: Food, grams: Double): NutritionValues {
        require(grams >= 0.0) { "grams must be greater than or equal to 0." }
        return food.nutritionFor(grams).rounded()
    }

    override fun calculateNutritionTarget(profile: UserProfile): NutritionTarget {
        require(profile.isBodyMetricsValid()) { "Profile body metrics are invalid." }

        val bmr = when (profile.gender) {
            Gender.MALE -> {
                10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age + 5
            }

            Gender.FEMALE -> {
                10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age - 161
            }
        }

        val activityMultiplier = NutritionConstants.activityMultipliers
            .getValue(profile.activityLevel)
        val tdee = bmr * activityMultiplier

        val carbsCalories = tdee * NutritionConstants.DEFAULT_CARB_RATIO
        val proteinCalories = tdee * NutritionConstants.DEFAULT_PROTEIN_RATIO
        val fatCalories = tdee * NutritionConstants.DEFAULT_FAT_RATIO

        return NutritionTarget(
            tdeeCalories = tdee.round(1),
            targetCarbsGrams = (carbsCalories / NutritionConstants.CARB_KCAL_PER_GRAM).round(1),
            targetProteinGrams = (proteinCalories / NutritionConstants.PROTEIN_KCAL_PER_GRAM).round(1),
            targetFatGrams = (fatCalories / NutritionConstants.FAT_KCAL_PER_GRAM).round(1),
            carbsRatio = NutritionConstants.DEFAULT_CARB_RATIO,
            proteinRatio = NutritionConstants.DEFAULT_PROTEIN_RATIO,
            fatRatio = NutritionConstants.DEFAULT_FAT_RATIO
        )
    }

    override fun aggregateDailyStats(
        userId: String,
        date: String,
        records: List<DietRecord>,
        target: NutritionTarget
    ): DailyStats {
        val totalNutrition = records.fold(NutritionValues()) { acc, record ->
            acc + record.nutritionValues
        }.rounded()

        val totalMacroCalories =
            totalNutrition.carbs * NutritionConstants.CARB_KCAL_PER_GRAM +
                totalNutrition.protein * NutritionConstants.PROTEIN_KCAL_PER_GRAM +
                totalNutrition.fat * NutritionConstants.FAT_KCAL_PER_GRAM

        val carbsRatio = if (totalMacroCalories == 0.0) 0.0
        else (totalNutrition.carbs * NutritionConstants.CARB_KCAL_PER_GRAM / totalMacroCalories).round(3)

        val proteinRatio = if (totalMacroCalories == 0.0) 0.0
        else (totalNutrition.protein * NutritionConstants.PROTEIN_KCAL_PER_GRAM / totalMacroCalories).round(3)

        val fatRatio = if (totalMacroCalories == 0.0) 0.0
        else (totalNutrition.fat * NutritionConstants.FAT_KCAL_PER_GRAM / totalMacroCalories).round(3)

        return DailyStats(
            id = "${userId}_${date}",
            userId = userId,
            date = date,
            totalCalories = totalNutrition.calories,
            totalCarbs = totalNutrition.carbs,
            totalProtein = totalNutrition.protein,
            totalFat = totalNutrition.fat,
            carbsRatio = carbsRatio,
            proteinRatio = proteinRatio,
            fatRatio = fatRatio,
            targetCalories = target.tdeeCalories,
            targetCarbs = target.targetCarbsGrams,
            targetProtein = target.targetProteinGrams,
            targetFat = target.targetFatGrams,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun NutritionValues.rounded(): NutritionValues {
        return NutritionValues(
            calories = calories.round(1),
            carbs = carbs.round(1),
            protein = protein.round(1),
            fat = fat.round(1)
        )
    }

    private fun Double.round(scale: Int): Double {
        val factor = 10.0.pow(scale)
        return (this * factor).roundToInt() / factor
    }

    private fun Double.pow(scale: Int): Double {
        var result = 1.0
        repeat(scale) {
            result *= 10.0
        }
        return result
    }
}
