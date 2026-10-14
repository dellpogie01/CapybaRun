package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyGold
import com.example.ui.theme.CapyTextDark

@Composable
fun GameOverScreen(
    score: Int,
    bestScore: Int,
    isNewBest: Boolean,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "GAME OVER",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CapyTextDark
                    ),
                    modifier = Modifier.testTag("game_over_title")
                )

                if (isNewBest) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CapyGold
                    ) {
                        Text(
                            text = "🎉 NEW BEST! 🎉",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CapyTextDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Final Score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CapyTextDark.copy(alpha = 0.7f)
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CapyPrimary
                    ),
                    modifier = Modifier.testTag("final_score_value")
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Personal Best: $bestScore",
                    style = MaterialTheme.typography.titleMedium,
                    color = CapyTextDark
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = CapyPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                            .testTag("restart_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onHome,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                            .testTag("home_button")
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = CapyTextDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Home", color = CapyTextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
