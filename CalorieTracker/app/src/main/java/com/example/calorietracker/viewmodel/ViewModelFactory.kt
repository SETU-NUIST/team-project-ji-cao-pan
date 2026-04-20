package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.calorietracker.CalorieTrackerApplication

fun CreationExtras.calorieTrackerApplication(): CalorieTrackerApplication {
    return this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CalorieTrackerApplication
}
