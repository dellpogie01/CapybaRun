package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val score: Int,
    val duration: Long, // in milliseconds
    val distance: Float,
    val eggsCollected: Int,
    val flightTime: Long, // in milliseconds
    val timestamp: Long = System.currentTimeMillis(),
    val isNewBest: Boolean
)

@Entity(tableName = "game_statistics")
data class GameStatisticsEntity(
    @PrimaryKey val id: Int = 1,
    val totalGames: Int = 0,
    val totalEggs: Int = 0,
    val totalDistance: Float = 0f,
    val totalSurvivalTime: Long = 0L, // in milliseconds
    val totalFlightTime: Long = 0L, // in milliseconds
    val bestScore: Int = 0
)

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val equippedHat: String = "none" // "none", "explorer", "crown", "flower", "wizard"
)

@Entity(tableName = "unlockables")
data class UnlockableEntity(
    @PrimaryKey val hatId: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockRequirement: Int // e.g. eggs collected or score needed
)
