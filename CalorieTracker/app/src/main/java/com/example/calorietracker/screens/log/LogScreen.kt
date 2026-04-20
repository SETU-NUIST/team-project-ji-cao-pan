package com.example.calorietracker.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.components.EmptyStateCard
import com.example.calorietracker.components.InlineErrorCard
import com.example.calorietracker.components.LoadingState
import com.example.calorietracker.components.MetricCard
import com.example.calorietracker.components.SelectorRow
import com.example.calorietracker.components.WellnessTextField
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.domain.model.Food
import com.example.calorietracker.domain.model.MealType
import com.example.calorietracker.viewmodel.LogViewModel

@Composable
fun LogScreen(
    onRecordClick: (String) -> Unit,
    viewModel: LogViewModel = viewModel(factory = LogViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingState(message = "Loading food log...")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Log Food",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Search foods, enter grams, choose a meal category, and save a nutrition snapshot.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            WellnessTextField(
                label = "Food Search",
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                placeholder = "Try avocado, yogurt, salmon..."
            )
        }

        item {
            CustomFoodSection(
                isVisible = uiState.isCustomFoodFormVisible,
                isSaving = uiState.isSavingCustomFood,
                name = uiState.customFoodName,
                caloriesInput = uiState.customCaloriesInput,
                carbsInput = uiState.customCarbsInput,
                proteinInput = uiState.customProteinInput,
                fatInput = uiState.customFatInput,
                onShow = viewModel::showCustomFoodForm,
                onHide = viewModel::hideCustomFoodForm,
                onNameChange = viewModel::updateCustomFoodName,
                onCaloriesChange = viewModel::updateCustomCaloriesInput,
                onCarbsChange = viewModel::updateCustomCarbsInput,
                onProteinChange = viewModel::updateCustomProteinInput,
                onFatChange = viewModel::updateCustomFatInput,
                onSave = viewModel::saveCustomFood
            )
        }

        item {
            FoodResultsSection(
                foods = uiState.filteredFoods,
                selectedFoodId = uiState.selectedFoodId,
                onFoodClick = viewModel::selectFood
            )
        }

        uiState.selectedFood?.let { food ->
            item {
                SelectedFoodCard(food)
            }
        }

        item {
            WellnessTextField(
                label = "Portion (grams)",
                value = uiState.gramsInput,
                onValueChange = viewModel::updateGramsInput,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Meal Category", style = MaterialTheme.typography.titleMedium)
                SelectorRow(
                    options = MealType.entries.toList(),
                    selected = uiState.selectedMealType,
                    labelOf = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    onSelected = viewModel::selectMealType
                )
            }
        }

        uiState.errorMessage?.let { message ->
            item { InlineErrorCard(message) }
        }

        uiState.savedMessage?.let { message ->
            item {
                Text(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Button(
                onClick = viewModel::saveRecord,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save Record")
            }
        }

        item {
            Text(
                text = "Today's records",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (uiState.todayRecords.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No records yet",
                    subtitle = "After you save a meal, the log list and detail flow will appear here."
                )
            }
        } else {
            items(uiState.todayRecords, key = { it.id }) { record ->
                LogRecordRow(record = record, onClick = { onRecordClick(record.id) })
            }
        }
    }
}

@Composable
private fun CustomFoodSection(
    isVisible: Boolean,
    isSaving: Boolean,
    name: String,
    caloriesInput: String,
    carbsInput: String,
    proteinInput: String,
    fatInput: String,
    onShow: () -> Unit,
    onHide: () -> Unit,
    onNameChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Custom Food",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Create your own food entry when search results do not have what you need.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!isVisible) {
            OutlinedButton(
                onClick = onShow,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text("Add Custom Food")
            }
            return
        }

        WellnessTextField(
            label = "Food Name",
            value = name,
            onValueChange = onNameChange,
            placeholder = "Example: Homemade chicken wrap"
        )
        WellnessTextField(
            label = "Calories per 100 g",
            value = caloriesInput,
            onValueChange = onCaloriesChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        WellnessTextField(
            label = "Carbs per 100 g",
            value = carbsInput,
            onValueChange = onCarbsChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        WellnessTextField(
            label = "Protein per 100 g",
            value = proteinInput,
            onValueChange = onProteinChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        WellnessTextField(
            label = "Fat per 100 g",
            value = fatInput,
            onValueChange = onFatChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                onClick = onHide,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save Food")
            }
        }
    }
}

@Composable
private fun FoodResultsSection(
    foods: List<Food>,
    selectedFoodId: String?,
    onFoodClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Food Results", style = MaterialTheme.typography.titleMedium)
        if (foods.isEmpty()) {
            EmptyStateCard(
                title = "No foods available",
                subtitle = "Add a custom food above, or seed the Firestore foods collection for shared options."
            )
        } else {
            foods.forEach { food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (selectedFoodId == food.id) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { onFoodClick(food.id) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(food.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = buildString {
                                append("${food.caloriesPer100g.toInt()} kcal per 100 g")
                                if (!food.isBaseFood) append(" • Custom")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (selectedFoodId == food.id) "Selected" else "Pick",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedFoodCard(food: Food) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(food.name, style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Nutrition snapshot uses this 100 g base and saves exact values at record time.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard("Kcal", "${food.caloriesPer100g.toInt()}", modifier = Modifier.weight(1f))
            MetricCard("Carbs", "${food.carbsPer100g.toInt()} g", modifier = Modifier.weight(1f))
            MetricCard("Protein", "${food.proteinPer100g.toInt()} g", modifier = Modifier.weight(1f))
            MetricCard("Fat", "${food.fatPer100g.toInt()} g", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LogRecordRow(
    record: DietRecord,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = record.foodName,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "${record.mealType.name.lowercase().replaceFirstChar(Char::uppercase)} • ${record.grams.toInt()} g",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tap to see the saved nutrition snapshot",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
