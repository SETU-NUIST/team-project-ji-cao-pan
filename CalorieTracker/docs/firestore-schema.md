# Firestore Schema Design

## Collection: `users`

Document id: Firebase Auth `uid`

```json
{
  "id": "uid",
  "email": "user@example.com",
  "username": "alex",
  "gender": "MALE",
  "age": 28,
  "heightCm": 175.0,
  "weightKg": 68.5,
  "activityLevel": "MODERATE",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

Purpose:
- Store account profile and body metrics used for TDEE and macro target calculation.

## Subcollection: `users/{uid}/dietRecords`

Document id: generated record id

```json
{
  "id": "record_001",
  "userId": "uid",
  "foodId": "food_apple",
  "foodName": "Apple",
  "mealType": "BREAKFAST",
  "grams": 180.0,
  "consumedAt": 1710723600000,
  "consumedDate": "2026-03-18",
  "calories": 93.6,
  "carbs": 25.2,
  "protein": 0.54,
  "fat": 0.36,
  "isCustomFood": false,
  "createdAt": 1710723600000,
  "updatedAt": 1710723600000
}
```

Purpose:
- Save immutable nutrition snapshot at record time.
- Keep daily, weekly, and monthly aggregation independent from later food edits.

Recommended indexes:
- `consumedDate` ascending
- `consumedAt` descending
- composite index: `consumedDate` ascending + `mealType` ascending

## Collection: `foods`

Document id: generated food id

```json
{
  "id": "food_apple",
  "name": "Apple",
  "nameKeywords": ["apple", "app", "水果", "pingguo"],
  "caloriesPer100g": 52.0,
  "carbsPer100g": 14.0,
  "proteinPer100g": 0.3,
  "fatPer100g": 0.2,
  "isBaseFood": true,
  "createdBy": "system",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

Purpose:
- Hold built-in foods and user-created foods.
- Support fuzzy search with `nameKeywords`.

Recommended indexes:
- `name` ascending
- `createdBy` ascending
- `isBaseFood` ascending

## Collection: `dailyStats`

Document id format: `{uid}_{yyyy-MM-dd}`

```json
{
  "id": "uid_2026-03-18",
  "userId": "uid",
  "date": "2026-03-18",
  "totalCalories": 1850.0,
  "totalCarbs": 220.0,
  "totalProtein": 112.0,
  "totalFat": 58.0,
  "carbsRatio": 0.48,
  "proteinRatio": 0.24,
  "fatRatio": 0.28,
  "targetCalories": 2100.0,
  "targetCarbs": 262.5,
  "targetProtein": 131.25,
  "targetFat": 58.33,
  "updatedAt": 1710727200000
}
```

Purpose:
- Cache daily aggregation for fast dashboard and statistics rendering.
- Can be recalculated whenever records change.

## Security Model

- `users/{uid}`: only owner can read/write.
- `users/{uid}/dietRecords/{recordId}`: only owner can read/write.
- `foods/{foodId}`:
  - base foods readable by all authenticated users.
  - custom foods writable only by creator.
- `dailyStats/{id}`: only owner can read/write.

## Sync Strategy

- Profile update triggers target recalculation.
- Diet record create/update/delete triggers same-day `dailyStats` rebuild.
- Built-in food seed data can be inserted once by admin script or app bootstrap routine.
