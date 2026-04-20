package com.example.calorietracker.data.repository

import com.example.calorietracker.data.firebase.awaitValue
import com.example.calorietracker.data.firebase.toDietRecordOrNull
import com.example.calorietracker.data.firebase.toFirestoreMap
import com.example.calorietracker.data.firebase.toFoodOrNull
import com.example.calorietracker.data.firebase.toUserProfileOrNull
import com.example.calorietracker.data.model.AuthenticatedUser
import com.example.calorietracker.data.model.SessionSnapshot
import com.example.calorietracker.domain.calculator.NutritionCalculator
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Food
import com.example.calorietracker.domain.model.MealType
import com.example.calorietracker.domain.model.UserProfile
import com.example.calorietracker.util.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class CloudBackedCalorieTrackerRepository(
    private val calculator: NutritionCalculator
) : CalorieTrackerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val sessionLoading = MutableStateFlow(false)

    private val authUserFlow: StateFlow<AuthenticatedUser?> =
        callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser?.toAuthenticatedUser())
            }
            auth.addAuthStateListener(listener)
            trySend(auth.currentUser?.toAuthenticatedUser())
            awaitClose { auth.removeAuthStateListener(listener) }
        }.stateIn(scope, SharingStarted.Eagerly, auth.currentUser?.toAuthenticatedUser())

    private val currentProfileFlow: StateFlow<UserProfile?> =
        authUserFlow.flatMapLatest { user ->
            if (user == null) {
                flowOf<UserProfile?>(null)
            } else {
                callbackFlow<UserProfile?> {
                    val registration = firestore.collection(USERS_COLLECTION)
                        .document(user.id)
                        .addSnapshotListener { snapshot, _ ->
                            trySend(snapshot?.toUserProfileOrNull())
                        }
                    awaitClose { registration.remove() }
                }
            }
        }.stateIn(scope, SharingStarted.Eagerly, null)

    override val sessionState: StateFlow<SessionSnapshot> =
        combine(authUserFlow, currentProfileFlow, sessionLoading) { user, profile, isLoading ->
            SessionSnapshot(
                isLoading = isLoading,
                currentUser = user,
                isProfileComplete = user != null && profile != null
            )
        }.stateIn(scope, SharingStarted.Eagerly, SessionSnapshot())

    override fun observeCurrentUserProfile(): Flow<UserProfile?> = currentProfileFlow

    override fun observeFoods(): Flow<List<Food>> {
        return authUserFlow.flatMapLatest { user ->
            callbackFlow {
                val registration = firestore.collection(FOODS_COLLECTION)
                    .orderBy("name")
                    .addSnapshotListener { snapshot, _ ->
                        val foods = snapshot?.documents
                            ?.mapNotNull { it.toFoodOrNull() }
                            ?.filter { it.isBaseFood || (user != null && it.createdBy == user.id) }
                            .orEmpty()
                        trySend(foods)
                    }
                awaitClose { registration.remove() }
            }
        }
    }

    override fun observeCurrentUserRecords(): Flow<List<DietRecord>> {
        return authUserFlow.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val registration = firestore.collection(USERS_COLLECTION)
                        .document(user.id)
                        .collection(RECORDS_COLLECTION)
                        .orderBy("consumedAt", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, _ ->
                            trySend(snapshot?.documents?.mapNotNull { it.toDietRecordOrNull() }.orEmpty())
                        }
                    awaitClose { registration.remove() }
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runFirebaseCall {
        auth.signInWithEmailAndPassword(email.trim(), password).awaitValue()
        Unit
    }

    override suspend fun register(username: String, email: String, password: String): Result<Unit> = runFirebaseCall {
        auth.createUserWithEmailAndPassword(email.trim(), password).awaitValue()
        auth.currentUser?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(username.trim())
                .build()
        )?.awaitValue()
        Unit
    }

    override suspend fun signOut() {
        sessionLoading.value = true
        auth.signOut()
        sessionLoading.value = false
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> = runFirebaseCall {
        val currentUser = auth.currentUser ?: error("Please sign in before saving your profile.")
        val payload = profile.copy(
            id = currentUser.uid,
            email = currentUser.email.orEmpty(),
            username = currentUser.displayName ?: profile.username.ifBlank { currentUser.email.orEmpty().substringBefore('@') }
        ).toFirestoreMap(
            username = currentUser.displayName ?: profile.username.ifBlank { currentUser.email.orEmpty().substringBefore('@') },
            email = currentUser.email.orEmpty()
        )
        firestore.collection(USERS_COLLECTION)
            .document(currentUser.uid)
            .set(payload)
            .awaitValue()
        Unit
    }

    override suspend fun addDietRecord(
        food: Food,
        grams: Double,
        mealType: MealType
    ): Result<DietRecord> = runFirebaseCall {
        val currentUser = auth.currentUser ?: error("Please sign in before saving a food record.")
        val nutrition = calculator.calculateFoodNutrition(food, grams)
        val now = System.currentTimeMillis()
        val record = DietRecord(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            foodId = food.id,
            foodName = food.name,
            mealType = mealType,
            grams = grams,
            consumedAt = now,
            consumedDate = DateUtils.todayKey(),
            calories = nutrition.calories,
            carbs = nutrition.carbs,
            protein = nutrition.protein,
            fat = nutrition.fat,
            isCustomFood = !food.isBaseFood,
            createdAt = now,
            updatedAt = now
        )
        firestore.collection(USERS_COLLECTION)
            .document(currentUser.uid)
            .collection(RECORDS_COLLECTION)
            .document(record.id)
            .set(record.toFirestoreMap())
            .awaitValue()
        record
    }

    override suspend fun getRecord(recordId: String): DietRecord? {
        val currentUser = auth.currentUser ?: return null
        return runCatching {
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .collection(RECORDS_COLLECTION)
                .document(recordId)
                .get()
                .awaitValue()
                .toDietRecordOrNull()
        }.getOrNull()
    }

    private suspend fun <T> runFirebaseCall(block: suspend () -> T): Result<T> {
        sessionLoading.value = true
        return runCatching { block() }.also { sessionLoading.value = false }
    }

    private fun com.google.firebase.auth.FirebaseUser.toAuthenticatedUser(): AuthenticatedUser {
        return AuthenticatedUser(
            id = uid,
            email = email.orEmpty(),
            username = displayName ?: email.orEmpty().substringBefore('@').ifBlank { "User" }
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val FOODS_COLLECTION = "foods"
        const val RECORDS_COLLECTION = "dietRecords"
    }
}
