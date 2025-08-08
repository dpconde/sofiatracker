package com.dpconde.sofiatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dpconde.sofiatracker.presentation.navigation.SofiaTrackerNavigation
import com.dpconde.sofiatracker.core.designsystem.theme.SofiaTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SofiaTrackerTheme {
                SofiaTrackerNavigation()
            }
        }
    }
}