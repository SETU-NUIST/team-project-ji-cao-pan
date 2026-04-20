package com.example.calorietracker.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

fun Throwable.toAuthErrorMessage(isRegistration: Boolean): String = when (this) {
    is FirebaseAuthWeakPasswordException ->
        "Choose a stronger password with at least 6 characters."

    is FirebaseAuthUserCollisionException ->
        "That email address is already registered. Try signing in instead."

    is FirebaseAuthInvalidUserException ->
        "No account was found for that email address."

    is FirebaseAuthInvalidCredentialsException ->
        if (isRegistration) {
            "Enter a valid email address and password."
        } else {
            "That email or password is incorrect."
        }

    is FirebaseTooManyRequestsException ->
        "Too many authentication attempts were made from this device. Please wait a moment and try again."

    is FirebaseNetworkException ->
        if (isRegistration) {
            "Firebase could not verify this sign-up request. Check the device internet connection, make sure Google Play services are available, and confirm this app's SHA-1 and SHA-256 fingerprints are registered in Firebase."
        } else {
            "A network error interrupted sign-in. Check the device connection and try again."
        }

    is FirebaseAuthException -> when (errorCode) {
        "CONFIGURATION_NOT_FOUND" ->
            "Firebase Authentication is not fully configured yet. Enable Email/Password sign-in and verify that google-services.json matches this Android app."

        "APP_NOT_AUTHORIZED" ->
            "This Android build is not authorized with Firebase yet. Add the app's SHA fingerprints in Firebase Console and refresh google-services.json if the app registration changed."

        "OPERATION_NOT_ALLOWED" ->
            "Email and password sign-in is disabled in Firebase Console."

        else -> message ?: "Authentication failed. Please try again."
    }

    else -> message ?: "Authentication failed. Please try again."
}
