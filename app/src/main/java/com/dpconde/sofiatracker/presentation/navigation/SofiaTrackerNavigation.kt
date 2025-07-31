package com.dpconde.sofiatracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dpconde.sofiatracker.presentation.addevent.AddEventScreen
import com.dpconde.sofiatracker.presentation.main.MainScreen
import com.dpconde.sofiatracker.domain.model.EventType

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
                onNavigateToAddEvent = { eventType ->
                    when (eventType) {
                        EventType.SLEEP -> navController.navigate(SofiaTrackerScreens.AddSleep.route)
                        EventType.EAT -> navController.navigate(SofiaTrackerScreens.AddEat.route)
                        EventType.POOP -> navController.navigate(SofiaTrackerScreens.AddPoop.route)
                    }
                }
            )
        }
        
        composable(SofiaTrackerScreens.AddSleep.route) {
            AddEventScreen(
                eventType = EventType.SLEEP,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(SofiaTrackerScreens.AddEat.route) {
            AddEventScreen(
                eventType = EventType.EAT,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(SofiaTrackerScreens.AddPoop.route) {
            AddEventScreen(
                eventType = EventType.POOP,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class SofiaTrackerScreens(val route: String) {
    object Main : SofiaTrackerScreens("main")
    object AddSleep : SofiaTrackerScreens("add_sleep")
    object AddEat : SofiaTrackerScreens("add_eat")
    object AddPoop : SofiaTrackerScreens("add_poop")
}