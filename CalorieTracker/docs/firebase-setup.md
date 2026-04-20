# Firebase Setup Notes

This project now uses Firebase as the app data source.

## What Is Wired In

- Firebase Authentication for login and registration
- Cloud Firestore for:
  - `users`
  - `users/{uid}/dietRecords`
  - `foods`

The repository keeps the existing nutrition calculation logic in the app and only moves account/data storage to Firebase.

## How To Enable Firebase Mode

1. Create a Firebase project.
2. Add an Android app with package name:
   - `com.example.calorietracker`
3. Enable:
   - Authentication -> Email/Password
   - Cloud Firestore
4. Add the Android app fingerprints in Firebase Console:
   - Run `./gradlew signingReport` from the `CalorieTracker` project
   - Copy the debug `SHA-1` and `SHA-256` values into Firebase Console -> Project settings -> Your apps -> Android app
5. Download `google-services.json`.
6. Place it in:
   - `app/google-services.json`
7. Sync and rebuild the project.

When the file exists, the Gradle `google-services` plugin is applied automatically and the app reads its foods directly from Firestore.

## Android Auth Verification Notes

Firebase Authentication can run an app-verification check before sign-in or sign-up requests complete on Android. If that verification falls back to reCAPTCHA, the device and Firebase project setup both matter.

- Use an emulator or device with working internet access.
- Prefer a Google Play-enabled emulator image when testing Firebase Auth flows.
- Make sure the debug app fingerprints are registered in Firebase, especially `SHA-256`.
- If you manually restrict the Firebase API key, allow Firebase Auth reCAPTCHA traffic for `calorietracker-8f885.firebaseapp.com`.

If registration logs show a reCAPTCHA or app-verification network error, the Kotlin auth call is usually fine and the missing piece is one of the items above.

## Firestore Structure

See:

- `docs/firestore-schema.md`

The current cloud repository follows the same schema direction as that file.
