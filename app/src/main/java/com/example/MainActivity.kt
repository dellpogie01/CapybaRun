package com.example

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameScreenState
import com.example.ui.screens.*
import com.example.ui.theme.CapybarunTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CapybarunTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CapybarunApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CapybarunApp(viewModel: GameViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val bestScore by viewModel.bestScore.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val unlockables by viewModel.unlockables.collectAsStateWithLifecycle()

    val soundEnabled = settings?.soundEnabled ?: true
    val equippedHat = settings?.equippedHat ?: "none"
    val totalEggs = statistics?.totalEggs ?: 0
    val bestScoreValue = bestScore ?: 0

    // Dynamic orientation: Landscape for gameplay, Portrait for menus
    LaunchedEffect(screenState) {
        if (screenState == GameScreenState.PLAYING) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    when (screenState) {
        GameScreenState.SPLASH -> {
            SplashScreen(
                onSplashFinished = { viewModel.setScreenState(GameScreenState.HOME) }
            )
        }
        GameScreenState.HOME -> {
            HomeScreen(
                bestScore = bestScoreValue,
                soundEnabled = soundEnabled,
                equippedHat = equippedHat,
                onPlayClicked = { viewModel.setScreenState(GameScreenState.PLAYING) },
                onStatsClicked = { viewModel.setScreenState(GameScreenState.STATS) },
                onTutorialClicked = { viewModel.setScreenState(GameScreenState.TUTORIAL) },
                onToggleSound = { viewModel.toggleSound() }
            )
        }
        GameScreenState.PLAYING -> {
            GameScreen(
                soundEnabled = soundEnabled,
                equippedHat = equippedHat,
                bestScore = bestScoreValue,
                onRecordGameSession = { score, duration, distance, eggs, flightTime ->
                    viewModel.recordGameSession(score, duration, distance, eggs, flightTime)
                },
                onHomeClicked = { viewModel.setScreenState(GameScreenState.HOME) },
                soundManager = viewModel.soundManager
            )
        }
        GameScreenState.PAUSED, GameScreenState.GAMEOVER -> {
            // Handled directly inside GameScreen to prevent orientation churn
            GameScreen(
                soundEnabled = soundEnabled,
                equippedHat = equippedHat,
                bestScore = bestScoreValue,
                onRecordGameSession = { score, duration, distance, eggs, flightTime ->
                    viewModel.recordGameSession(score, duration, distance, eggs, flightTime)
                },
                onHomeClicked = { viewModel.setScreenState(GameScreenState.HOME) },
                soundManager = viewModel.soundManager
            )
        }
        GameScreenState.WARDROBE -> {
            WardrobeScreen(
                unlockables = unlockables,
                equippedHat = equippedHat,
                totalEggs = totalEggs,
                bestScore = bestScoreValue,
                onEquip = { hatId -> viewModel.equipHat(hatId) },
                onBack = { viewModel.setScreenState(GameScreenState.HOME) }
            )
        }
        GameScreenState.STATS -> {
            StatisticsScreen(
                stats = statistics,
                onBack = { viewModel.setScreenState(GameScreenState.HOME) }
            )
        }
        GameScreenState.TUTORIAL -> {
            TutorialScreen(
                onBack = { viewModel.setScreenState(GameScreenState.HOME) }
            )
        }
        GameScreenState.AI_STUDIO -> {
            HomeScreen(
                bestScore = bestScoreValue,
                soundEnabled = soundEnabled,
                equippedHat = equippedHat,
                onPlayClicked = { viewModel.setScreenState(GameScreenState.PLAYING) },
                onStatsClicked = { viewModel.setScreenState(GameScreenState.STATS) },
                onTutorialClicked = { viewModel.setScreenState(GameScreenState.TUTORIAL) },
                onToggleSound = { viewModel.toggleSound() }
            )
        }
    }
}
