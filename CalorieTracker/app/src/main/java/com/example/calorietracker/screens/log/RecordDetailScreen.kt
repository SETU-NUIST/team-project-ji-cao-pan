package com.example.calorietracker.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import com.example.calorietracker.components.EmptyStateCard
import com.example.calorietracker.components.LoadingState
import com.example.calorietracker.components.MetricCard
import com.example.calorietracker.util.DateUtils
import com.example.calorietracker.viewmodel.RecordDetailViewModel

@Composable
fun RecordDetailScreen(
    recordId: String,
    onBack: () -> Unit
) {
    val viewModel: RecordDetailViewModel = viewModel(
        factory = RecordDetailViewModel.provideFactory(recordId)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingState(message = "Loading record detail...")
        return
    }

    val record = uiState.record
    if (record == null) {
        EmptyStateCard(
            title = "Record unavailable",
            subtitle = uiState.errorMessage ?: "This record no longer exists."
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Record Detail",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Saved nutrition values remain fixed even if the food list changes later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(record.foodName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${record.mealType.name.lowercase().replaceFirstChar(Char::uppercase)} • ${record.grams.toInt()} g • ${DateUtils.friendlyDate(record.consumedAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Calories", "${record.calories.toInt()} kcal", modifier = Modifier.weight(1f))
                MetricCard("Carbs", "${record.carbs.toInt()} g", modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Protein", "${record.protein.toInt()} g", modifier = Modifier.weight(1f))
                MetricCard("Fat", "${record.fat.toInt()} g", modifier = Modifier.weight(1f))
            }
        }
    }
}
