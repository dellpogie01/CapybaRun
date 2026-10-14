package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.game.GameEngine
import com.example.game.GamePlayState
import com.example.game.Obstacle
import com.example.game.ObstacleType
import com.example.ui.theme.CapyGold
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyTextDark
import kotlinx.coroutines.isActive
import kotlin.math.sin

@Composable
fun GameScreen(
    soundEnabled: Boolean,
    equippedHat: String,
    bestScore: Int,
    onRecordGameSession: (score: Int, duration: Long, distance: Float, eggs: Int, flightTime: Long) -> Unit,
    onHomeClicked: () -> Unit,
    soundManager: SoundManager
) {
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    val gameEngine = remember(screenWidthPx, screenHeightPx) {
        GameEngine(screenWidthPx, screenHeightPx).apply {
            reset(startInReady = true)
        }
    }

    // Reactive state observed by Compose UI
    var gameState by remember { mutableStateOf(gameEngine.state) }
    var currentScore by remember { mutableStateOf(0) }
    var flightTimeRemaining by remember { mutableStateOf(0f) }
    var isFlying by remember { mutableStateOf(false) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var hasRecordedSession by remember { mutableStateOf(false) }

    // Real-time 60FPS Game Loop using withFrameNanos
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { currentTime ->
                val dt = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                if (gameEngine.state == GamePlayState.PLAYING) {
                    val wasFlying = gameEngine.isFlying
                    val running = gameEngine.update(dt)

                    // Sound trigger when flight starts
                    if (!wasFlying && gameEngine.isFlying) {
                        soundManager.playFlightStart(soundEnabled)
                    }

                    // Collision triggered game over
                    if (!running) {
                        soundManager.playGameOver(soundEnabled)
                        if (!hasRecordedSession) {
                            hasRecordedSession = true
                            onRecordGameSession(
                                gameEngine.currentScore,
                                gameEngine.survivalDuration,
                                gameEngine.distanceTraveled,
                                gameEngine.eggsCollectedCount,
                                (gameEngine.maxFlightDuration - gameEngine.flightTimeRemaining).toLong().coerceAtLeast(0L) * 1000L
                            )
                        }
                    }
                }

                // Push engine values into reactive state for UI
                gameState = gameEngine.state
                currentScore = gameEngine.currentScore
                flightTimeRemaining = gameEngine.flightTimeRemaining
                isFlying = gameEngine.isFlying
                frameTick++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (gameEngine.state == GamePlayState.READY || gameEngine.state == GamePlayState.PLAYING) {
                            val didJump = gameEngine.onUserTap()
                            if (didJump) {
                                soundManager.playJump(soundEnabled)
                            }
                        }
                        tryAwaitRelease()
                    }
                )
            }
    ) {
        // Main Game Canvas: Background, Obstacles, Collectibles, Capybara, Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Force redraw on each frame tick
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            val width = size.width
            val height = size.height
            val groundY = gameEngine.groundY

            // --- 1. SKY GRADIENT ---
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF81D4FA), // Morning Sky Blue
                        Color(0xFFE1F5FE), // Soft Horizon Mist
                        Color(0xFFF1F8E9)  // Gentle Earth Horizon
                    ),
                    startY = 0f,
                    endY = groundY
                ),
                size = Size(width, groundY)
            )

            // --- 2. MOVING CLOUDS (Gentle drift across sky) ---
            val cloudScroll = gameEngine.distanceTraveled * 0.08f + frameTick * 0.35f
            drawMovingClouds(
                width = width,
                groundY = groundY,
                scrollOffset = cloudScroll
            )

            // --- 3. FLYING BIRDS (Gently flapping and gliding across sky) ---
            val birdScroll = gameEngine.distanceTraveled * 0.22f + frameTick * 1.6f
            drawFlyingBirds(
                width = width,
                groundY = groundY,
                scrollOffset = birdScroll,
                frameTick = frameTick
            )

            // --- 4. SEAMLESS ROLLING HILLS (Distant Parallax ~0.18x speed) ---
            val distantDist = gameEngine.distanceTraveled * 0.18f
            drawSeamlessHills(
                width = width,
                groundY = groundY,
                hillHeight = 110f,
                scrollOffset = distantDist,
                color = Color(0xFFA5D6A7).copy(alpha = 0.85f),
                wavelength = width * 0.65f
            )

            // --- 3. SEAMLESS NEAR HILLS & TREES (Midground Parallax ~0.45x speed) ---
            val nearDist = gameEngine.distanceTraveled * 0.42f
            drawSeamlessHills(
                width = width,
                groundY = groundY,
                hillHeight = 65f,
                scrollOffset = nearDist,
                color = Color(0xFF81C784),
                wavelength = width * 0.45f
            )

            // Midground Trees on Near Hills
            drawSeamlessTrees(
                width = width,
                groundY = groundY - 35f,
                scrollOffset = nearDist
            )

            // --- 4. GROUND & FOREGROUND TERRAIN ---
            // Earth fill under ground line
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8D6E63), // Rich Soil Brown
                        Color(0xFF5D4037)  // Deep Earth
                    ),
                    startY = groundY,
                    endY = height
                ),
                topLeft = Offset(0f, groundY),
                size = Size(width, height - groundY)
            )

            // Ground Top Line
            drawLine(
                color = CapyTextDark,
                start = Offset(0f, groundY),
                end = Offset(width, groundY),
                strokeWidth = 5f
            )

            // Continuous scrolling grass blades along ground line (1.0x speed)
            val grassStep = 45f
            val grassShift = (gameEngine.distanceTraveled * 1.0f) % grassStep
            var gx = -grassShift
            while (gx < width + grassStep) {
                // Grass tuft
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(gx, groundY),
                    end = Offset(gx - 4f, groundY - 10f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0xFF81C784),
                    start = Offset(gx + 4f, groundY),
                    end = Offset(gx + 6f, groundY - 12f),
                    strokeWidth = 2.5f
                )
                // Small ground pebbles
                drawCircle(
                    color = Color(0xFFD7CCC8),
                    radius = 2f,
                    center = Offset(gx + 20f, groundY + 12f)
                )
                gx += grassStep
            }

            // --- 5. MOVING OBSTACLES (Right to Left) ---
            for (obs in gameEngine.obstacles) {
                drawObstacle(obs)
            }

            // --- 6. MAGICAL EGGS ---
            for (egg in gameEngine.eggs) {
                if (!egg.collected) {
                    drawMagicalEgg(egg.x, egg.y, egg.size)
                }
            }

            // --- 7. PARTICLES ---
            for (p in gameEngine.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(p.x, p.y)
                )
            }

            // --- 8. ANIMATED CAPYBARA ---
            drawCapybara(
                engine = gameEngine,
                equippedHat = equippedHat
            )
        }

        // --- TOP OVERLAY: Score, Flight Meter, Pause Button ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCORE: $currentScore",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CapyTextDark,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.testTag("game_score_text")
                        )
                    }
                }

                if (isFlying) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CapyGold,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = "✨ FLIGHT: ${String.format("%.1f", flightTimeRemaining)}s",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CapyTextDark
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Upper Right Corner: High Score & Pause Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CapyGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "BEST ${maxOf(bestScore, currentScore)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CapyTextDark,
                                letterSpacing = 0.8.sp
                            ),
                            modifier = Modifier.testTag("game_high_score_text")
                        )
                    }
                }

                // Pause Button (Only enabled during PLAYING)
                if (gameState == GamePlayState.PLAYING) {
                    IconButton(
                        onClick = {
                            gameEngine.state = GamePlayState.PAUSED
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.92f), shape = CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = CapyTextDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- READY STATE OVERLAY: Tap to Start ---
        if (gameState == GamePlayState.READY) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "TAP ANYWHERE TO JUMP!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CapyPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Avoid obstacles & collect golden eggs for magical flight",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CapyTextDark.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        // --- PAUSED STATE OVERLAY ---
        if (gameState == GamePlayState.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.width(320.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CapyTextDark
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { gameEngine.state = GamePlayState.PLAYING },
                            colors = ButtonDefaults.buttonColors(containerColor = CapyPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("resume_button")
                        ) {
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                hasRecordedSession = false
                                gameEngine.reset(startInReady = false)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pause_restart_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CapyTextDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restart Run", color = CapyTextDark, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = onHomeClicked,
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("pause_home_button")
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = CapyTextDark.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Main Menu", color = CapyTextDark.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- GAME OVER OVERLAY ---
        if (gameState == GamePlayState.GAME_OVER) {
            val isNewBest = currentScore > bestScore
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 10.dp,
                    modifier = Modifier.width(360.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "GAME OVER",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CapyTextDark
                            ),
                            modifier = Modifier.testTag("game_over_title")
                        )

                        if (isNewBest) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CapyGold
                            ) {
                                Text(
                                    text = "🎉 NEW BEST! 🎉",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = CapyTextDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Score", style = MaterialTheme.typography.bodySmall, color = CapyTextDark.copy(alpha = 0.6f))
                                Text(
                                    text = "$currentScore",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CapyPrimary
                                    ),
                                    modifier = Modifier.testTag("final_score_value")
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Best", style = MaterialTheme.typography.bodySmall, color = CapyTextDark.copy(alpha = 0.6f))
                                Text(
                                    text = "${maxOf(bestScore, currentScore)}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CapyTextDark
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    hasRecordedSession = false
                                    gameEngine.reset(startInReady = false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CapyPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1.2f).height(48.dp).testTag("restart_button")
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Again", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onHomeClicked,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).height(48.dp).testTag("home_button")
                            ) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = CapyTextDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Home", color = CapyTextDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER RENDERING FUNCTIONS ---

// --- CLOUDS & BIRDS RENDERING ---

private data class BirdSpec(
    val offsetX: Float,
    val yRatio: Float,
    val scale: Float,
    val phase: Float
)

private fun DrawScope.drawCartoonCloud(
    x: Float,
    y: Float,
    scale: Float = 1.0f,
    alpha: Float = 0.9f
) {
    val cloudColor = Color.White.copy(alpha = alpha)
    val shadowColor = Color(0xFFB3E5FC).copy(alpha = alpha * 0.45f)

    // Base soft shadow
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(x, y + 8f * scale),
        size = Size(86f * scale, 26f * scale),
        cornerRadius = CornerRadius(13f * scale, 13f * scale)
    )
    // Cloud pill base
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(x, y + 4f * scale),
        size = Size(86f * scale, 26f * scale),
        cornerRadius = CornerRadius(13f * scale, 13f * scale)
    )
    // Fluffy cloud puffs
    drawCircle(
        color = cloudColor,
        radius = 18f * scale,
        center = Offset(x + 22f * scale, y + 10f * scale)
    )
    drawCircle(
        color = cloudColor,
        radius = 24f * scale,
        center = Offset(x + 44f * scale, y + 3f * scale)
    )
    drawCircle(
        color = cloudColor,
        radius = 17f * scale,
        center = Offset(x + 66f * scale, y + 12f * scale)
    )
}

private fun DrawScope.drawMovingClouds(
    width: Float,
    groundY: Float,
    scrollOffset: Float
) {
    val cloudConfigs = listOf(
        Triple(0f, groundY * 0.10f, 1.25f),
        Triple(270f, groundY * 0.23f, 0.85f),
        Triple(560f, groundY * 0.13f, 1.10f),
        Triple(850f, groundY * 0.25f, 0.95f),
        Triple(1150f, groundY * 0.15f, 1.20f)
    )
    val totalSpan = 1400f
    val baseShift = scrollOffset % totalSpan

    for ((baseX, cy, scale) in cloudConfigs) {
        var cx = baseX - baseShift
        while (cx < -150f) cx += totalSpan
        while (cx < width + 150f) {
            drawCartoonCloud(cx, cy, scale = scale)
            cx += totalSpan
        }
    }
}

private fun DrawScope.drawSingleBird(
    x: Float,
    y: Float,
    wingAngle: Float,
    scale: Float = 1.0f
) {
    val birdColor = Color(0xFF263238).copy(alpha = 0.82f)
    val wingLift = sin(wingAngle.toDouble()).toFloat() * 6.5f * scale

    val birdPath = Path().apply {
        moveTo(x - 12f * scale, y - wingLift)
        quadraticTo(x - 5f * scale, y - 4f * scale - wingLift * 0.4f, x, y)
        quadraticTo(x + 5f * scale, y - 4f * scale - wingLift * 0.4f, x + 12f * scale, y - wingLift)
        quadraticTo(x, y + 2.5f * scale, x - 12f * scale, y - wingLift)
        close()
    }
    drawPath(path = birdPath, color = birdColor)

    // Tiny bird head
    drawCircle(
        color = birdColor,
        radius = 2.2f * scale,
        center = Offset(x - 2f * scale, y - 1f)
    )
}

private fun DrawScope.drawFlyingBirds(
    width: Float,
    groundY: Float,
    scrollOffset: Float,
    frameTick: Long
) {
    val birdSpecs = listOf(
        BirdSpec(0f, 0.18f, 1.1f, 0f),
        BirdSpec(42f, 0.14f, 0.9f, 0.9f),
        BirdSpec(72f, 0.22f, 0.85f, 1.6f),
        BirdSpec(500f, 0.26f, 0.95f, 2.3f),
        BirdSpec(545f, 0.20f, 0.8f, 3.1f)
    )
    val totalSpan = 1200f
    val baseShift = scrollOffset % totalSpan

    for (spec in birdSpecs) {
        val cy = groundY * spec.yRatio
        var bx = spec.offsetX - baseShift
        while (bx < -60f) bx += totalSpan
        while (bx < width + 60f) {
            val wingAngle = frameTick * 0.14f + spec.phase
            val bobY = cy + sin((frameTick * 0.06f + spec.phase).toDouble()).toFloat() * 4f
            drawSingleBird(bx, bobY, wingAngle = wingAngle, scale = spec.scale)
            bx += totalSpan
        }
    }
}

private fun DrawScope.drawSeamlessHills(
    width: Float,
    groundY: Float,
    hillHeight: Float,
    scrollOffset: Float,
    color: Color,
    wavelength: Float
) {
    val path = Path()
    val baseOffset = scrollOffset % wavelength

    path.moveTo(-baseOffset, groundY)
    var x = -baseOffset
    while (x <= width + wavelength * 2f) {
        val midX = x + wavelength * 0.5f
        val endX = x + wavelength
        path.quadraticTo(midX, groundY - hillHeight, endX, groundY)
        x = endX
    }
    path.lineTo(width, groundY + 10f)
    path.lineTo(0f, groundY + 10f)
    path.close()

    drawPath(path = path, color = color)
}

private fun DrawScope.drawSeamlessTrees(
    width: Float,
    groundY: Float,
    scrollOffset: Float
) {
    val spacing = 160f
    val shift = scrollOffset % spacing
    var tx = -shift
    var idx = 0
    while (tx <= width + spacing) {
        val treeType = idx % 2
        if (treeType == 0) {
            // Pine tree
            drawRect(color = Color(0xFF6D4C41), topLeft = Offset(tx + 12f, groundY - 15f), size = Size(8f, 18f))
            drawCircle(color = Color(0xFF2E7D32), radius = 22f, center = Offset(tx + 16f, groundY - 30f))
            drawCircle(color = Color(0xFF388E3C), radius = 16f, center = Offset(tx + 16f, groundY - 42f))
        } else {
            // Round bush tree
            drawRect(color = Color(0xFF5D4037), topLeft = Offset(tx + 14f, groundY - 10f), size = Size(6f, 14f))
            drawCircle(color = Color(0xFF43A047), radius = 18f, center = Offset(tx + 17f, groundY - 22f))
        }
        tx += spacing
        idx++
    }
}

private fun DrawScope.drawObstacle(obs: Obstacle) {
    when (obs.type) {
        ObstacleType.ROCK -> {
            // Stone with highlights
            drawRoundRect(
                color = Color(0xFF78909C),
                topLeft = Offset(obs.x, obs.y),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(10f, 10f)
            )
            // Rock highlight facet
            drawRoundRect(
                color = Color(0xFFB0BEC5),
                topLeft = Offset(obs.x + 4f, obs.y + 4f),
                size = Size(obs.width * 0.45f, obs.height * 0.4f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
        ObstacleType.LOG -> {
            // Wooden Log with wood-grain rings
            drawRoundRect(
                color = Color(0xFF6D4C41),
                topLeft = Offset(obs.x, obs.y),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawCircle(
                color = Color(0xFFD7CCC8),
                radius = obs.height * 0.38f,
                center = Offset(obs.x + obs.width - 12f, obs.y + obs.height * 0.5f)
            )
            drawCircle(
                color = Color(0xFF8D6E63),
                radius = obs.height * 0.18f,
                center = Offset(obs.x + obs.width - 12f, obs.y + obs.height * 0.5f)
            )
        }
        ObstacleType.TREE_STUMP -> {
            // Forest tree stump
            drawRoundRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(obs.x, obs.y),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Rings on top of stump
            drawOval(
                color = Color(0xFFBCAAA4),
                topLeft = Offset(obs.x + 3f, obs.y + 2f),
                size = Size(obs.width - 6f, 12f)
            )
            drawOval(
                color = Color(0xFF8D6E63),
                topLeft = Offset(obs.x + 10f, obs.y + 4f),
                size = Size(obs.width - 20f, 6f)
            )
        }
        ObstacleType.BUSH -> {
            // Round green bush with berries
            val cx = obs.x + obs.width * 0.5f
            val cy = obs.y + obs.height * 0.55f
            val r = obs.width * 0.46f
            drawCircle(color = Color(0xFF2E7D32), radius = r, center = Offset(cx, cy))
            drawCircle(color = Color(0xFF388E3C), radius = r * 0.75f, center = Offset(cx - 5f, cy - 6f))
            // Red berries
            drawCircle(color = Color(0xFFE53935), radius = 3.5f, center = Offset(cx - 8f, cy - 2f))
            drawCircle(color = Color(0xFFE53935), radius = 3.5f, center = Offset(cx + 8f, cy + 2f))
        }
        ObstacleType.PRICKLY_BUSH -> {
            // Spiky thicket
            val cx = obs.x + obs.width * 0.5f
            val cy = obs.y + obs.height * 0.55f
            val r = obs.width * 0.46f
            drawCircle(color = Color(0xFF1B5E20), radius = r, center = Offset(cx, cy))
            // Yellow warning thorns/flowers
            drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(cx, cy - 8f))
            drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(cx - 10f, cy + 4f))
            drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(cx + 10f, cy + 4f))
        }
        ObstacleType.PUDDLE -> {
            // Water puddle
            drawOval(
                color = Color(0xFF42A5F5),
                topLeft = Offset(obs.x, obs.y),
                size = Size(obs.width, obs.height)
            )
            // Ripple highlight
            drawOval(
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(obs.x + 8f, obs.y + 2f),
                size = Size(obs.width * 0.5f, obs.height * 0.4f)
            )
        }
        ObstacleType.FLOATING_ROCK -> {
            // Mystic elevated floating rock
            drawRoundRect(
                color = Color(0xFF546E7A),
                topLeft = Offset(obs.x, obs.y),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFF78909C),
                topLeft = Offset(obs.x + 4f, obs.y + 3f),
                size = Size(obs.width * 0.55f, obs.height * 0.45f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Soft glowing mystic underside
            drawCircle(
                color = CapyGold.copy(alpha = 0.4f),
                radius = 8f,
                center = Offset(obs.x + obs.width * 0.5f, obs.y + obs.height + 4f)
            )
        }
    }
}

private fun DrawScope.drawMagicalEgg(x: Float, y: Float, size: Float) {
    // Outer golden glow
    drawCircle(
        color = CapyGold.copy(alpha = 0.35f),
        radius = size * 1.5f,
        center = Offset(x, y)
    )
    // Egg body
    drawOval(
        color = CapyGold,
        topLeft = Offset(x - size * 0.7f, y - size),
        size = Size(size * 1.4f, size * 2.0f)
    )
    // Shiny inner highlight
    drawOval(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(x - size * 0.4f, y - size * 0.7f),
        size = Size(size * 0.5f, size * 0.9f)
    )
}

private fun DrawScope.drawCapybara(
    engine: GameEngine,
    equippedHat: String
) {
    val cx = engine.capyX
    val cy = engine.capyY
    val cw = engine.capyWidth
    val ch = engine.capyHeight

    // Running leg animation cycle (only while on ground and running)
    val isGrounded = engine.isGrounded
    val isFlying = engine.isFlying
    val legCycle = if (isGrounded && engine.state == GamePlayState.PLAYING) {
        sin(engine.distanceTraveled * 0.06f) * 12f
    } else {
        0f
    }

    // Leaping pose vs running pose:
    // When airborne and moving up: tilt head up
    // When airborne and falling: extend legs
    val jumpTilt = if (!isGrounded && !isFlying) {
        if (engine.yVelocity < 0f) -4f else 4f
    } else {
        0f
    }

    // Capybara Body (Chunky, cute, rounded)
    drawRoundRect(
        color = CapyPrimary,
        topLeft = Offset(cx, cy + jumpTilt * 0.5f),
        size = Size(cw - 8f, ch),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // Snout / Head
    drawRoundRect(
        color = CapyPrimary,
        topLeft = Offset(cx + cw - 26f, cy - 8f + jumpTilt),
        size = Size(32f, 32f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Ears
    drawRoundRect(
        color = Color(0xFF8D5B34),
        topLeft = Offset(cx + cw - 28f, cy - 18f + jumpTilt),
        size = Size(10f, 12f),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // Cartoon Eye with highlight
    drawCircle(
        color = CapyTextDark,
        radius = 3.5f,
        center = Offset(cx + cw - 10f, cy - 1f + jumpTilt)
    )
    drawCircle(
        color = Color.White,
        radius = 1.2f,
        center = Offset(cx + cw - 11f, cy - 2f + jumpTilt)
    )

    // Cute Nose Dot
    drawCircle(
        color = CapyTextDark,
        radius = 2.5f,
        center = Offset(cx + cw + 4f, cy + 4f + jumpTilt)
    )

    // Legs
    if (isFlying) {
        // Flying pose: little legs tucked back, cape or golden glow
        drawLine(
            color = Color(0xFF8D5B34),
            start = Offset(cx + 16f, cy + ch - 2f),
            end = Offset(cx + 8f, cy + ch + 4f),
            strokeWidth = 6f
        )
        drawLine(
            color = Color(0xFF8D5B34),
            start = Offset(cx + 46f, cy + ch - 2f),
            end = Offset(cx + 38f, cy + ch + 4f),
            strokeWidth = 6f
        )
    } else if (isGrounded) {
        // 4 Legs running cycle
        // Back leg
        drawLine(
            color = Color(0xFF8D5B34),
            start = Offset(cx + 18f, cy + ch - 2f),
            end = Offset(cx + 18f - legCycle, cy + ch + 12f),
            strokeWidth = 6f
        )
        // Front leg
        drawLine(
            color = Color(0xFF8D5B34),
            start = Offset(cx + 46f, cy + ch - 2f),
            end = Offset(cx + 46f + legCycle, cy + ch + 12f),
            strokeWidth = 6f
        )
    } else {
        // In the air (jumping): legs curled in natural jump arc
        drawLine(
            color = Color(0xFF8D5B34),
            start = Offset(cx + 18f, cy + ch - 2f),
            end = Offset(cx + 14f, cy + ch + 6f),
            strokeWidth = 6f
        )
        drawLine(
            color = Color(0xFF8D5B34),
            start = Offset(cx + 46f, cy + ch - 2f),
            end = Offset(cx + 52f, cy + ch + 6f),
            strokeWidth = 6f
        )
    }

    // Hat rendering if equipped
    val headX = cx + cw - 26f
    val headY = cy - 8f + jumpTilt
    when (equippedHat) {
        "explorer" -> {
            drawRect(
                color = Color(0xFF4A3525),
                topLeft = Offset(headX - 6f, headY - 8f),
                size = Size(42f, 7f)
            )
            drawRoundRect(
                color = Color(0xFF6D4C41),
                topLeft = Offset(headX + 2f, headY - 18f),
                size = Size(26f, 12f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        "crown" -> {
            drawRoundRect(
                color = CapyGold,
                topLeft = Offset(headX + 2f, headY - 16f),
                size = Size(28f, 14f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawCircle(color = Color.Red, radius = 2.5f, center = Offset(headX + 7f, headY - 12f))
            drawCircle(color = Color.Cyan, radius = 2.5f, center = Offset(headX + 16f, headY - 12f))
            drawCircle(color = Color.Red, radius = 2.5f, center = Offset(headX + 25f, headY - 12f))
        }
        "flower" -> {
            drawCircle(color = Color.Magenta, radius = 7f, center = Offset(headX + 10f, headY - 10f))
            drawCircle(color = Color.Yellow, radius = 3.5f, center = Offset(headX + 10f, headY - 10f))
        }
        "wizard" -> {
            drawRoundRect(
                color = Color(0xFF3F37C9),
                topLeft = Offset(headX + 2f, headY - 24f),
                size = Size(24f, 22f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawCircle(color = CapyGold, radius = 3.5f, center = Offset(headX + 14f, headY - 22f))
        }
    }
}
