package com.dpconde.sofiatracker.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dpconde.sofiatracker.presentation.main.MainScreen
import com.dpconde.sofiatracker.presentation.statistics.StatisticsScreen
import com.dpconde.sofiatracker.presentation.settings.SettingsScreen
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.usecase.GetBabyNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    getBabyNameUseCase: GetBabyNameUseCase
) : ViewModel() {
    
    val babyName: StateFlow<String> = getBabyNameUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Sofía"
        )
}

@Composable
fun MainNavigationScreen(
    onNavigateToAddEvent: (EventType) -> Unit,
    onNavigateToEditEvent: (com.dpconde.sofiatracker.domain.model.Event) -> Unit,
    viewModel: MainNavigationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val babyName by viewModel.babyName.collectAsState()
    
    
    val bottomNavItems = listOf(
        BottomNavItem.Home.copy(title = babyName),
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