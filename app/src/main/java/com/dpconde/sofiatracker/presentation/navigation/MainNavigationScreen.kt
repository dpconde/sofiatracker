package com.dpconde.sofiatracker.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dpconde.sofiatracker.presentation.main.MainScreen
import com.dpconde.sofiatracker.presentation.statistics.StatisticsScreen
import com.dpconde.sofiatracker.presentation.settings.SettingsScreen
import com.dpconde.sofiatracker.domain.model.EventType

@Composable
fun MainNavigationScreen(
    onNavigateToAddEvent: (EventType) -> Unit,
    onNavigateToEditEvent: (com.dpconde.sofiatracker.domain.model.Event) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Statistics,
        BottomNavItem.Settings
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                MainScreen(
                    onNavigateToAddEvent = onNavigateToAddEvent,
                    onNavigateToEditEvent = onNavigateToEditEvent
                )
            }
            
            composable(BottomNavItem.Statistics.route) {
                StatisticsScreen()
            }
            
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}