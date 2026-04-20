package com.example.calorietracker.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.components.InlineErrorCard
import com.example.calorietracker.components.LoadingState
import com.example.calorietracker.components.MetricCard
import com.example.calorietracker.components.SelectorRow
import com.example.calorietracker.components.WellnessTextField
import com.example.calorietracker.domain.model.ActivityLevel
import com.example.calorietracker.domain.model.Gender
import com.example.calorietracker.viewmodel.ProfileViewModel

@Composable
fun ProfileSetupScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    ProfileFormScreen(
        title = "Profile Setup",
        subtitle = "Complete these details before entering the main app. They directly drive your TDEE target.",
        primaryAction = "Save and Continue",
        onSignOut = onSignOut,
        viewModel = viewModel
    )
}

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    ProfileFormScreen(
        title = "Profile",
        subtitle = "Update your body data so the calorie and macro targets stay accurate.",
        primaryAction = "Save Changes",
        onSignOut = onSignOut,
        viewModel = viewModel
    )
}

@Composable
private fun ProfileFormScreen(
    title: String,
    subtitle: String,
    primaryAction: String,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingState(
            modifier = Modifier.fillMaxSize(),
            message = "Loading profile..."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WellnessTextField(
                    label = "Age",
                    value = uiState.age,
                    onValueChange = viewModel::updateAge,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                WellnessTextField(
                    label = "Height (cm)",
                    value = uiState.heightCm,
                    onValueChange = viewModel::updateHeight,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        item {
            WellnessTextField(
                label = "Weight (kg)",
                value = uiState.weightKg,
                onValueChange = viewModel::updateWeight,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Gender", style = MaterialTheme.typography.titleMedium)
                SelectorRow(
                    options = Gender.entries.toList(),
                    selected = uiState.gender,
                    labelOf = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    onSelected = viewModel::updateGender
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Activity Level", style = MaterialTheme.typography.titleMedium)
                ActivityLevel.entries.forEach { level ->
                    ActivityLevelCard(
                        title = level.label(),
                        subtitle = level.description(),
                        selected = uiState.activityLevel == level,
                        onClick = { viewModel.updateActivityLevel(level) }
                    )
                }
            }
        }

        uiState.targetPreview?.let { target ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TDEE Preview", style = MaterialTheme.typography.titleMedium)
                    MetricCard(
                        title = "Daily calories",
                        value = "${target.tdeeCalories.toInt()} kcal"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Carbs",
                            value = "${target.targetCarbsGrams.toInt()} g",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Protein",
                            value = "${target.targetProteinGrams.toInt()} g",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Fat",
                            value = "${target.targetFatGrams.toInt()} g",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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
                onClick = viewModel::saveProfile,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Saving..." else primaryAction)
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text("Sign Out")
            }
        }
    }
}

@Composable
private fun ActivityLevelCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun ActivityLevel.label(): String = when (this) {
    ActivityLevel.SEDENTARY -> "Sedentary"
    ActivityLevel.LIGHT -> "Lightly Active"
    ActivityLevel.MODERATE -> "Moderately Active"
    ActivityLevel.HIGH -> "Highly Active"
}

private fun ActivityLevel.description(): String = when (this) {
    ActivityLevel.SEDENTARY -> "Little to no exercise during the week."
    ActivityLevel.LIGHT -> "Light activity 1 to 3 days per week."
    ActivityLevel.MODERATE -> "Moderate exercise on most days."
    ActivityLevel.HIGH -> "Intense training or active work routine."
}
