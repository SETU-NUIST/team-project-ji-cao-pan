package com.example.calorietracker.data.firebase

import com.example.calorietracker.domain.model.ActivityLevel
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Food
import com.example.calorietracker.domain.model.Gender
import com.example.calorietracker.domain.model.MealType
import com.example.calorietracker.domain.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot

internal fun UserProfile.toFirestoreMap(username: String, email: String): Map<String, Any> {
    val now = System.currentTimeMillis()
    return mapOf(
        "id" to id,
        "email" to email,
        "username" to username,
        "gender" to gender.name,
        "age" to age,
        "heightCm" to heightCm,
        "weightKg" to weightKg,
        "activityLevel" to activityLevel.name,
        "createdAt" to (createdAt.takeIf { it > 0 } ?: now),
        "updatedAt" to now
    )
}

internal fun Food.toFirestoreMap(): Map<String, Any> {
    val now = System.currentTimeMillis()
    return mapOf(
        "id" to id,
        "name" to name,
        "nameKeywords" to nameKeywords,
        "caloriesPer100g" to caloriesPer100g,
        "carbsPer100g" to carbsPer100g,
        "proteinPer100g" to proteinPer100g,
        "fatPer100g" to fatPer100g,
        "isBaseFood" to isBaseFood,
        "createdBy" to createdBy,
        "createdAt" to (createdAt.takeIf { it > 0 } ?: now),
        "updatedAt" to now
    )
}

internal fun DietRecord.toFirestoreMap(): Map<String, Any> {
    val now = System.currentTimeMillis()
    return mapOf(
        "id" to id,
        "userId" to userId,
        "foodId" to foodId,
        "foodName" to foodName,
        "mealType" to mealType.name,
        "grams" to grams,
        "consumedAt" to consumedAt,
        "consumedDate" to consumedDate,
        "calories" to calories,
        "carbs" to carbs,
        "protein" to protein,
        "fat" to fat,
        "isCustomFood" to isCustomFood,
        "createdAt" to (createdAt.takeIf { it > 0 } ?: now),
        "updatedAt" to now
    )
}

internal fun DocumentSnapshot.toUserProfileOrNull(): UserProfile? {
    if (!exists()) return null
    val genderName = getString("gender") ?: return null
    val activityLevelName = getString("activityLevel") ?: return null
    return UserProfile(
        id = getString("id").orEmpty().ifBlank { id },
        email = getString("email").orEmpty(),
        username = getString("username").orEmpty(),
        gender = enumValueOfOrDefault(genderName, Gender.FEMALE),
        age = getLong("age")?.toInt() ?: 18,
        heightCm = getDouble("heightCm") ?: 170.0,
        weightKg = getDouble("weightKg") ?: 65.0,
        activityLevel = enumValueOfOrDefault(activityLevelName, ActivityLevel.SEDENTARY),
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

internal fun DocumentSnapshot.toFoodOrNull(): Food? {
    if (!exists()) return null
    return Food(
        id = getString("id").orEmpty().ifBlank { id },
        name = getString("name").orEmpty(),
        nameKeywords = (get("nameKeywords") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        caloriesPer100g = getDouble("caloriesPer100g") ?: 0.0,
        carbsPer100g = getDouble("carbsPer100g") ?: 0.0,
        proteinPer100g = getDouble("proteinPer100g") ?: 0.0,
        fatPer100g = getDouble("fatPer100g") ?: 0.0,
        isBaseFood = getBoolean("isBaseFood") ?: false,
        createdBy = getString("createdBy").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

internal fun DocumentSnapshot.toDietRecordOrNull(): DietRecord? {
    if (!exists()) return null
    val mealTypeName = getString("mealType") ?: return null
    return DietRecord(
        id = getString("id").orEmpty().ifBlank { id },
        userId = getString("userId").orEmpty(),
        foodId = getString("foodId").orEmpty(),
        foodName = getString("foodName").orEmpty(),
        mealType = enumValueOfOrDefault(mealTypeName, MealType.BREAKFAST),
        grams = getDouble("grams") ?: 0.0,
        consumedAt = getLong("consumedAt") ?: 0L,
        consumedDate = getString("consumedDate").orEmpty(),
        calories = getDouble("calories") ?: 0.0,
        carbs = getDouble("carbs") ?: 0.0,
        protein = getDouble("protein") ?: 0.0,
        fat = getDouble("fat") ?: 0.0,
        isCustomFood = getBoolean("isCustomFood") ?: false,
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

private inline fun <reified T : Enum<T>> enumValueOfOrDefault(
    value: String,
    default: T
): T = runCatching { enumValueOf<T>(value) }.getOrDefault(default)
