package com.dpconde.sofiatracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.feature.home.addevent.AddEatScreen
import com.dpconde.sofiatracker.feature.home.addevent.AddPoopScreen
import com.dpconde.sofiatracker.feature.home.addevent.AddSleepScreen

@Composable
fun SofiaTrackerNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SofiaTrackerScreens.Main.route
    ) {
        composable(SofiaTrackerScreens.Main.route) {
            MainNavigationScreen(
                onNavigateToAddEvent = { eventType ->
                    when (eventType) {
                        EventType.SLEEP -> navController.navigate(SofiaTrackerScreens.AddSleep.route)
                        EventType.EAT -> navController.navigate(SofiaTrackerScreens.AddEat.route)
                        EventType.POOP -> navController.navigate(SofiaTrackerScreens.AddPoop.route)
                    }
                },
                onNavigateToEditEvent = { event ->
                    when (event.type) {
                        EventType.SLEEP -> navController.navigate(SofiaTrackerScreens.EditSleep.createRoute(event.id))
                        EventType.EAT -> navController.navigate(SofiaTrackerScreens.EditEat.createRoute(event.id))
                        EventType.POOP -> navController.navigate(SofiaTrackerScreens.EditPoop.createRoute(event.id))
                    }
                }
            )
        }
        
        composable(SofiaTrackerScreens.AddSleep.route) {
            AddSleepScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(SofiaTrackerScreens.AddEat.route) {
            AddEatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(SofiaTrackerScreens.AddPoop.route) {
            AddPoopScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = SofiaTrackerScreens.EditSleep.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            AddSleepScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                editEventId = eventId
            )
        }
        
        composable(
            route = SofiaTrackerScreens.EditEat.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            AddEatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                editEventId = eventId
            )
        }
        
        composable(
            route = SofiaTrackerScreens.EditPoop.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            AddPoopScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                editEventId = eventId
            )
        }
    }
}

sealed class SofiaTrackerScreens(val route: String) {
    object Main : SofiaTrackerScreens("main")
    object AddSleep : SofiaTrackerScreens("add_sleep")
    object AddEat : SofiaTrackerScreens("add_eat")
    object AddPoop : SofiaTrackerScreens("add_poop")
    object EditSleep : SofiaTrackerScreens("edit_sleep/{eventId}") {
        fun createRoute(eventId: Long) = "edit_sleep/$eventId"
    }
    object EditEat : SofiaTrackerScreens("edit_eat/{eventId}") {
        fun createRoute(eventId: Long) = "edit_eat/$eventId"
    }
    object EditPoop : SofiaTrackerScreens("edit_poop/{eventId}") {
        fun createRoute(eventId: Long) = "edit_poop/$eventId"
    }
}