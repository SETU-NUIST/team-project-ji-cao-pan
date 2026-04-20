package com.example.calorietracker.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.components.EmptyStateCard
import com.example.calorietracker.components.GoalProgressCard
import com.example.calorietracker.components.LoadingState
import com.example.calorietracker.components.MetricCard
import com.example.calorietracker.domain.model.DietRecord
import com.example.calorietracker.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingState(message = "Loading dashboard...")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Monitor today's intake against your TDEE target.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val stats = uiState.dailyStats
        val target = uiState.target
        if (stats != null && target != null) {
            item {
                GoalProgressCard(
                    consumedCalories = stats.totalCalories,
                    targetCalories = target.tdeeCalories
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Carbs",
                        value = "${stats.totalCarbs.toInt()} g",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Protein",
                        value = "${stats.totalProtein.toInt()} g",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Fat",
                        value = "${stats.totalFat.toInt()} g",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Today's meals",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (uiState.todayRecords.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No meals logged today",
                    subtitle = "Use the Log tab to search for a food, enter grams, and save your first record."
                )
            }
        } else {
            items(uiState.todayRecords, key = { it.id }) { record ->
                HomeRecordRow(record)
            }
        }
    }
}

@Composable
private fun HomeRecordRow(record: DietRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
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
            text = "${record.calories.toInt()} kcal",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
