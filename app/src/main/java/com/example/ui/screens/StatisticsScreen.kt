package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GameStatisticsEntity
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyTextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    stats: GameStatisticsEntity?,
    onBack: () -> Unit
) {
    val totalGames = stats?.totalGames ?: 0
    val bestScore = stats?.bestScore ?: 0
    val totalEggs = stats?.totalEggs ?: 0
    val totalDistance = stats?.totalDistance ?: 0f
    val totalFlightTimeSec = (stats?.totalFlightTime ?: 0L) / 1000L
    val totalSurvivalSec = (stats?.totalSurvivalTime ?: 0L) / 1000L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("stats_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatCard(title = "Personal Best Score", value = "$bestScore", highlight = true)
            }
            item {
                StatCard(title = "Total Games Played", value = "$totalGames")
            }
            item {
                StatCard(title = "Total Eggs Collected", value = "$totalEggs 🥚")
            }
            item {
                StatCard(title = "Total Distance Run", value = "${String.format("%.1f", totalDistance / 100f)} m")
            }
            item {
                StatCard(title = "Total Flight Time", value = "$totalFlightTimeSec s")
            }
            item {
                StatCard(title = "Total Survival Time", value = "$totalSurvivalSec s")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, highlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (highlight) CapyPrimary else MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (highlight) MaterialTheme.colorScheme.onPrimary else CapyTextDark
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (highlight) MaterialTheme.colorScheme.onPrimary else CapyPrimary
            )
        }
    }
}
