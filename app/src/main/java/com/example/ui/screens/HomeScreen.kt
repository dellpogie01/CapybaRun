package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CapyGold
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyTextDark

@Composable
fun HomeScreen(
    bestScore: Int,
    soundEnabled: Boolean,
    equippedHat: String = "none",
    onPlayClicked: () -> Unit,
    onWardrobeClicked: () -> Unit = {},
    onStatsClicked: () -> Unit,
    onTutorialClicked: () -> Unit,
    onToggleSound: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Kid-friendly AI Forest Background
        Image(
            painter = painterResource(id = R.drawable.img_capy_forest_bg),
            contentDescription = "Magical Forest Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Soft gradient overlay for contrast and readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        // Vertical Portrait Layout:
        // CapybaRun box at TOP, unobstructed scenery in the CENTER,
        // Play button and the 3 action buttons docked at the BOTTOM.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // --- TOP: CapybaRun Box ---
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(top = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 18.dp, horizontal = 20.dp)
                ) {
                    Text(
                        text = "CapybaRun",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = CapyPrimary,
                            letterSpacing = 1.2.sp
                        ),
                        modifier = Modifier.testTag("app_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to Jump & Avoid Obstacles!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CapyTextDark.copy(alpha = 0.75f)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CapyGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Best Score: $bestScore",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CapyTextDark
                            ),
                            modifier = Modifier.testTag("home_best_score")
                        )
                    }
                }
            }

            // Spacious center gap revealing the vibrant kid-friendly forest background
            Spacer(modifier = Modifier.weight(1f))

            // --- BOTTOM: Play Button & 3 Action Buttons ---
            // 1. Primary Play Button
            Button(
                onClick = onPlayClicked,
                colors = ButtonDefaults.buttonColors(containerColor = CapyPrimary),
                shape = RoundedCornerShape(22.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(64.dp)
                    .testTag("play_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Horizontal Row: Stats, How to Play, Sound On (Icons ONLY, NO text!)
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Stats Icon Button
                FilledTonalIconButton(
                    onClick = onStatsClicked,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("stats_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.95f),
                        contentColor = CapyPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = "Stats",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 2. How to Play Icon Button
                FilledTonalIconButton(
                    onClick = onTutorialClicked,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("tutorial_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.95f),
                        contentColor = CapyPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "How to Play",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 3. Sound Toggle Icon Button
                FilledTonalIconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("sound_toggle_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.95f),
                        contentColor = if (soundEnabled) CapyPrimary else Color(0xFF757575)
                    )
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (soundEnabled) "Sound On" else "Sound Off",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
