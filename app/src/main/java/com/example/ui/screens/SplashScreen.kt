package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyGold
import com.example.ui.theme.CapyTextDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Auto finish splash after 1.8 seconds
    LaunchedEffect(Unit) {
        delay(1800L)
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")
    val bounceAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFB703), // Sunny Orange-Yellow
                        Color(0xFFFB8500), // Vibrant Orange
                        Color(0xFFC68B59)  // Warm Caramel
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Illustrated Cute Capybara Canvas
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(bounceAnim),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    
                    // Body / Head base
                    drawCircle(
                        color = Color(0xFFE0A96D),
                        radius = 65f,
                        center = center
                    )
                    // Snout
                    drawOval(
                        color = Color(0xFFC68B59),
                        topLeft = Offset(center.x - 20f, center.y),
                        size = androidx.compose.ui.geometry.Size(60f, 40f)
                    )
                    // Ears
                    drawCircle(color = Color(0xFF8D5B34), radius = 14f, center = Offset(center.x - 45f, center.y - 35f))
                    drawCircle(color = Color(0xFF8D5B34), radius = 14f, center = Offset(center.x + 35f, center.y - 35f))
                    
                    // Eyes
                    drawCircle(color = CapyTextDark, radius = 6f, center = Offset(center.x - 20f, center.y - 15f))
                    drawCircle(color = Color.White, radius = 2f, center = Offset(center.x - 22f, center.y - 17f))

                    drawCircle(color = CapyTextDark, radius = 6f, center = Offset(center.x + 20f, center.y - 15f))
                    drawCircle(color = Color.White, radius = 2f, center = Offset(center.x + 18f, center.y - 17f))

                    // Nose / Snout dots
                    drawCircle(color = CapyTextDark, radius = 3f, center = Offset(center.x + 5f, center.y + 15f))
                    drawCircle(color = CapyTextDark, radius = 3f, center = Offset(center.x + 25f, center.y + 15f))

                    // Happy Smile
                    drawArc(
                        color = CapyTextDark,
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(center.x - 5f, center.y + 15f),
                        size = androidx.compose.ui.geometry.Size(30f, 20f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CapybaRun",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.testTag("splash_title")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Magical Adventures Await!",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFFFF3B0),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
