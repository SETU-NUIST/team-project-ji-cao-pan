package com.example.calorietracker

import android.app.Application
import com.example.calorietracker.data.AppContainer
import com.example.calorietracker.data.DefaultAppContainer

class CalorieTrackerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}
