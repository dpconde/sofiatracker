package com.dpconde.sofiatracker.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.ui.theme.SofiaTrackerTheme

@Composable
fun FabMenu(
    onEventTypeSelected: (EventType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(300),
        label = "fab_rotation"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Sub FABs
        if (isExpanded) {

            FabMenuItem(
                onClick = {
                    onEventTypeSelected(EventType.EAT)
                    isExpanded = false
                },
                text = "🍼 Eat",
            )

            FabMenuItem(
                onClick = {
                    onEventTypeSelected(EventType.SLEEP)
                    isExpanded = false
                },
                text = "😴 Sleep",
            )

            FabMenuItem(
                onClick = { 
                    onEventTypeSelected(EventType.POOP)
                    isExpanded = false
                },
                text = "💩 Poop",
            )

        }
        
        // Main FAB
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isExpanded) "Close menu" else "Add event",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FabMenuPreview() {
    SofiaTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FabMenu(
                onEventTypeSelected = { }
            )
        }
    }
}