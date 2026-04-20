package com.example.calorietracker.data.firebase

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> Task<T>.awaitValue(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { value ->
        continuation.resume(value)
    }.addOnFailureListener { error ->
        continuation.resumeWithException(error)
    }
}
