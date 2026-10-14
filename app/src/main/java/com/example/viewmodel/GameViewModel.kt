package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.CapybaDatabase
import com.example.data.GameRepository
import com.example.data.GameSettingsEntity
import com.example.data.GameStatisticsEntity
import com.example.data.UnlockableEntity
import com.example.game.GameScreenState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CapybaDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameDao())
    val soundManager = SoundManager(application)

    private val _screenState = MutableStateFlow(GameScreenState.HOME)
    val screenState: StateFlow<GameScreenState> = _screenState.asStateFlow()

    val bestScore: StateFlow<Int?> = repository.bestScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statistics: StateFlow<GameStatisticsEntity?> = repository.statistics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val settings: StateFlow<GameSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockables: StateFlow<List<UnlockableEntity>> = repository.unlockables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastGameScore = MutableStateFlow(0)
    val lastGameScore: StateFlow<Int> = _lastGameScore.asStateFlow()

    private val _isNewBest = MutableStateFlow(false)
    val isNewBest: StateFlow<Boolean> = _isNewBest.asStateFlow()

    fun setScreenState(state: GameScreenState) {
        _screenState.value = state
    }

    fun toggleSound() {
        val current = settings.value?.soundEnabled ?: true
        viewModelScope.launch {
            repository.updateSettings(
                soundEnabled = !current,
                hapticsEnabled = settings.value?.hapticsEnabled ?: true,
                equippedHat = settings.value?.equippedHat ?: "none",
                current = settings.value
            )
        }
    }

    fun equipHat(hatId: String) {
        viewModelScope.launch {
            repository.updateSettings(
                soundEnabled = settings.value?.soundEnabled ?: true,
                hapticsEnabled = settings.value?.hapticsEnabled ?: true,
                equippedHat = hatId,
                current = settings.value
            )
        }
    }

    fun recordGameSession(
        score: Int,
        duration: Long,
        distance: Float,
        eggs: Int,
        flightTime: Long
    ) {
        _lastGameScore.value = score
        val currentBest = bestScore.value ?: 0
        val newBest = score > currentBest
        _isNewBest.value = newBest

        viewModelScope.launch {
            repository.saveGameResult(
                score = score,
                duration = duration,
                distance = distance,
                eggsCollected = eggs,
                flightTime = flightTime,
                isNewBest = newBest
            )
            repository.updateStatisticsAfterRun(
                score = score,
                duration = duration,
                distance = distance,
                eggs = eggs,
                flight = flightTime,
                currentStats = statistics.value
            )

            // Check unlockables
            val totalEggs = (statistics.value?.totalEggs ?: 0) + eggs
            val bestSoFar = maxOf(currentBest, score)
            unlockables.value.forEach { item ->
                if (!item.isUnlocked) {
                    if (item.hatId == "explorer" && totalEggs >= item.unlockRequirement) {
                        repository.unlockHat(item.hatId)
                    } else if (item.hatId == "crown" && bestSoFar >= item.unlockRequirement) {
                        repository.unlockHat(item.hatId)
                    } else if (item.hatId == "flower" && totalEggs >= item.unlockRequirement) {
                        repository.unlockHat(item.hatId)
                    } else if (item.hatId == "wizard" && bestSoFar >= item.unlockRequirement) {
                        repository.unlockHat(item.hatId)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
