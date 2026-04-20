package com.example.calorietracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calorietracker.navigation.AppRoutes
import com.example.calorietracker.navigation.bottomDestinations
import com.example.calorietracker.screens.analytics.AnalyticsScreen
import com.example.calorietracker.screens.auth.AuthScreen
import com.example.calorietracker.screens.home.HomeScreen
import com.example.calorietracker.screens.log.LogScreen
import com.example.calorietracker.screens.log.RecordDetailScreen
import com.example.calorietracker.screens.profile.ProfileScreen
import com.example.calorietracker.screens.profile.ProfileSetupScreen
import com.example.calorietracker.viewmodel.SessionViewModel

@Composable
fun CalorieTrackerApp() {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory)
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomRoutes = bottomDestinations.map { it.route }
    val showBottomBar = currentRoute in bottomRoutes

    LaunchedEffect(sessionState.isLoading, sessionState.currentUser, sessionState.requiresProfileSetup, currentRoute) {
        when {
            sessionState.isLoading -> if (currentRoute != AppRoutes.splash) {
                navController.navigate(AppRoutes.splash) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }

            sessionState.currentUser == null -> if (currentRoute != AppRoutes.auth) {
                navController.navigate(AppRoutes.auth) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }

            sessionState.requiresProfileSetup -> if (currentRoute != AppRoutes.profileSetup) {
                navController.navigate(AppRoutes.profileSetup) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }

            currentRoute == AppRoutes.auth ||
                currentRoute == AppRoutes.profileSetup ||
                currentRoute == AppRoutes.splash -> {
                navController.navigate(AppRoutes.home) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.splash,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.splash) {
                SplashScreen()
            }
            composable(AppRoutes.auth) {
                AuthScreen()
            }
            composable(AppRoutes.profileSetup) {
                ProfileSetupScreen(
                    onSignOut = sessionViewModel::signOut
                )
            }
            composable(AppRoutes.home) {
                HomeScreen()
            }
            composable(AppRoutes.log) {
                LogScreen(
                    onRecordClick = { recordId ->
                        navController.navigate(AppRoutes.recordDetailRoute(recordId))
                    }
                )
            }
            composable(AppRoutes.analytics) {
                AnalyticsScreen()
            }
            composable(AppRoutes.profile) {
                ProfileScreen(
                    onSignOut = sessionViewModel::signOut
                )
            }
            composable(
                route = AppRoutes.recordDetail,
                arguments = listOf(navArgument(AppRoutes.recordIdArg) { type = NavType.StringType })
            ) { entry ->
                val recordId = entry.arguments?.getString(AppRoutes.recordIdArg).orEmpty()
                RecordDetailScreen(
                    recordId = recordId,
                    onBack = navController::navigateUp
                )
            }
        }
    }
}

