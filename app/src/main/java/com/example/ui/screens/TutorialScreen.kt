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
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyTextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Play", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tutorial_back_button")) {
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
                TutorialCard(
                    title = "1. Tap to Jump",
                    description = "Tap anywhere on the screen to make your capybara jump over obstacles like rocks, logs, bushes, and puddles."
                )
            }
            item {
                TutorialCard(
                    title = "2. Collect Magical Eggs",
                    description = "Keep an eye out for glowing golden eggs floating ahead. Collecting an egg activates 5 seconds of magical flight mode!"
                )
            }
            item {
                TutorialCard(
                    title = "3. Temporary Flight",
                    description = "While flying, you glide safely over all ground obstacles and can tap to ascend further. Watch the flight timer bar."
                )
            }
            item {
                TutorialCard(
                    title = "4. Survive & Score",
                    description = "The longer you survive and the further you run, the higher your score will climb. Try to beat your personal best!"
                )
            }
        }
    }
}

@Composable
fun TutorialCard(title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CapyPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CapyTextDark
            )
        }
    }
}
