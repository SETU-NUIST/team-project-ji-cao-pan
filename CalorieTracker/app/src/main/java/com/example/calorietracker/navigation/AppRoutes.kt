package com.example.calorietracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

object AppRoutes {
    const val splash = "splash"
    const val auth = "auth"
    const val profileSetup = "profile_setup"
    const val home = "home"
    const val log = "log"
    const val analytics = "analytics"
    const val profile = "profile"
    const val recordIdArg = "recordId"
    const val recordDetail = "record_detail/{$recordIdArg}"

    fun recordDetailRoute(recordId: String): String = "record_detail/$recordId"
}

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomDestinations = listOf(
    BottomDestination(AppRoutes.home, "Home", Icons.Outlined.Home),
    BottomDestination(AppRoutes.log, "Log", Icons.Outlined.ListAlt),
    BottomDestination(AppRoutes.analytics, "Analytics", Icons.Outlined.BarChart),
    BottomDestination(AppRoutes.profile, "Profile", Icons.Outlined.Person)
)
