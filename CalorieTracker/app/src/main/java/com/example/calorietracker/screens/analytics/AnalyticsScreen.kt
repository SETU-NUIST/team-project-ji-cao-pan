package com.example.calorietracker.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.components.EmptyStateCard
import com.example.calorietracker.components.LoadingState
import com.example.calorietracker.components.MacroRingChart
import com.example.calorietracker.components.MetricCard
import com.example.calorietracker.components.NutritionSegment
import com.example.calorietracker.components.SelectorRow
import com.example.calorietracker.components.TrendBarChart
import com.example.calorietracker.ui.theme.MintSupport
import com.example.calorietracker.ui.theme.RoseAccent
import com.example.calorietracker.ui.theme.RosePrimary
import com.example.calorietracker.viewmodel.AnalyticsPeriod
import com.example.calorietracker.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingState(message = "Building analytics...")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Review daily, weekly, and monthly intake patterns with target feedback.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SelectorRow(
                options = AnalyticsPeriod.entries.toList(),
                selected = uiState.selectedPeriod,
                labelOf = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                onSelected = viewModel::selectPeriod
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Consumed",
                    value = "${uiState.totalCalories.toInt()} kcal",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Target",
                    value = "${uiState.targetCalories.toInt()} kcal",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!uiState.hasData) {
            item {
                EmptyStateCard(
                    title = "No analytics yet",
                    subtitle = "Once you log meals, the chart and feedback will show your nutrition balance."
                )
            }
        } else {
            item {
                MacroRingChart(
                    segments = listOf(
                        NutritionSegment("Carbs", uiState.carbsRatio.toFloat(), RosePrimary),
                        NutritionSegment("Protein", uiState.proteinRatio.toFloat(), MintSupport),
                        NutritionSegment("Fat", uiState.fatRatio.toFloat(), RoseAccent)
                    ),
                    centerLabel = "${uiState.totalCalories.toInt()} kcal"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard("Carbs", "${uiState.totalCarbs.toInt()} g", modifier = Modifier.weight(1f))
                    MetricCard("Protein", "${uiState.totalProtein.toInt()} g", modifier = Modifier.weight(1f))
                    MetricCard("Fat", "${uiState.totalFat.toInt()} g", modifier = Modifier.weight(1f))
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Rationality Feedback", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = uiState.feedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Period Trend", style = MaterialTheme.typography.titleMedium)
                    TrendBarChart(
                        points = uiState.trend.map { it.label to it.calories }
                    )
                }
            }
        }
    }
}
