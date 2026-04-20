# Firestore Foods Seed Guide

The `Log` screen reads foods from Firestore only.
If the `foods` collection has one document, the app should only show that one document.

Create a top-level collection:

- `foods`

Each document should include these fields:

```json
{
  "id": "food_avocado_toast",
  "name": "Avocado Toast",
  "nameKeywords": ["avocado", "toast", "breakfast", "bread"],
  "caloriesPer100g": 160.0,
  "carbsPer100g": 18.0,
  "proteinPer100g": 5.0,
  "fatPer100g": 8.0,
  "isBaseFood": true,
  "createdBy": "system",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

## Recommended Starter Documents

### Document ID: `food_avocado_toast`

```json
{
  "id": "food_avocado_toast",
  "name": "Avocado Toast",
  "nameKeywords": ["avocado", "toast", "breakfast", "bread"],
  "caloriesPer100g": 160.0,
  "carbsPer100g": 18.0,
  "proteinPer100g": 5.0,
  "fatPer100g": 8.0,
  "isBaseFood": true,
  "createdBy": "system",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

### Document ID: `food_greek_yogurt`

```json
{
  "id": "food_greek_yogurt",
  "name": "Greek Yogurt",
  "nameKeywords": ["yogurt", "greek", "protein", "snack"],
  "caloriesPer100g": 97.0,
  "carbsPer100g": 3.9,
  "proteinPer100g": 10.0,
  "fatPer100g": 5.0,
  "isBaseFood": true,
  "createdBy": "system",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

### Document ID: `food_oatmilk_latte`

```json
{
  "id": "food_oatmilk_latte",
  "name": "Oatmilk Latte",
  "nameKeywords": ["latte", "coffee", "oatmilk", "drink"],
  "caloriesPer100g": 58.0,
  "carbsPer100g": 7.5,
  "proteinPer100g": 1.5,
  "fatPer100g": 2.0,
  "isBaseFood": true,
  "createdBy": "system",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

### Document ID: `food_salmon_rice`

```json
{
  "id": "food_salmon_rice",
  "name": "Salmon Rice Bowl",
  "nameKeywords": ["salmon", "rice", "bowl", "lunch", "dinner"],
  "caloriesPer100g": 182.0,
  "carbsPer100g": 16.0,
  "proteinPer100g": 12.0,
  "fatPer100g": 8.0,
  "isBaseFood": true,
  "createdBy": "system",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

### Document ID: `food_banana`

```json
{
  "id": "food_banana",
  "name": "Banana",
  "nameKeywords": ["banana", "fruit", "snack", "yellow"],
  "caloriesPer100g": 89.0,
  "carbsPer100g": 23.0,
  "proteinPer100g": 1.1,
  "fatPer100g": 0.3,
  "isBaseFood": true,
  "createdBy": "system",
  "createdAt": 1710720000000,
  "updatedAt": 1710720000000
}
```

## Optional Manual Console Steps

1. Open Firestore Database.
2. Create collection: `foods`
3. Add each document using the document IDs above.
4. Use matching field names and types.
