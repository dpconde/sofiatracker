package com.dpconde.sofiatracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dpconde.sofiatracker.presentation.addevent.AddEventScreen
import com.dpconde.sofiatracker.presentation.main.MainScreen

@Composable
fun SofiaTrackerNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SofiaTrackerScreens.Main.route
    ) {
        composable(SofiaTrackerScreens.Main.route) {
            MainScreen(
                onNavigateToAddEvent = {
                    navController.navigate(SofiaTrackerScreens.AddEvent.route)
                }
            )
        }
        
        composable(SofiaTrackerScreens.AddEvent.route) {
            AddEventScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class SofiaTrackerScreens(val route: String) {
    object Main : SofiaTrackerScreens("main")
    object AddEvent : SofiaTrackerScreens("add_event")
}