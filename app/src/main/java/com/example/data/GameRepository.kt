package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GameRepository(private val gameDao: GameDao) {
    val allRecords: Flow<List<GameRecordEntity>> = gameDao.getAllRecords()
    val bestScore: Flow<Int?> = gameDao.getBestScore()
    val statistics: Flow<GameStatisticsEntity?> = gameDao.getStatistics()
    val settings: Flow<GameSettingsEntity?> = gameDao.getSettings()
    val unlockables: Flow<List<UnlockableEntity>> = gameDao.getAllUnlockables()

    suspend fun saveGameResult(
        score: Int,
        duration: Long,
        distance: Float,
        eggsCollected: Int,
        flightTime: Long,
        isNewBest: Boolean
    ) {
        val record = GameRecordEntity(
            score = score,
            duration = duration,
            distance = distance,
            eggsCollected = eggsCollected,
            flightTime = flightTime,
            isNewBest = isNewBest
        )
        gameDao.insertRecord(record)
    }

    suspend fun updateStatisticsAfterRun(
        score: Int,
        duration: Long,
        distance: Float,
        eggs: Int,
        flight: Long,
        currentStats: GameStatisticsEntity?
    ) {
        val stats = currentStats ?: GameStatisticsEntity()
        val newBest = maxOf(stats.bestScore, score)
        val updated = stats.copy(
            totalGames = stats.totalGames + 1,
            totalEggs = stats.totalEggs + eggs,
            totalDistance = stats.totalDistance + distance,
            totalSurvivalTime = stats.totalSurvivalTime + duration,
            totalFlightTime = stats.totalFlightTime + flight,
            bestScore = newBest
        )
        gameDao.insertOrUpdateStatistics(updated)
    }

    suspend fun updateSettings(soundEnabled: Boolean, hapticsEnabled: Boolean, equippedHat: String, current: GameSettingsEntity?) {
        val settings = current ?: GameSettingsEntity()
        val updated = settings.copy(
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            equippedHat = equippedHat
        )
        gameDao.insertOrUpdateSettings(updated)
    }

    suspend fun unlockHat(hatId: String) {
        gameDao.unlockHat(hatId)
    }
}
