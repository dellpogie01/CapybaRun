package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GameRecordEntity>>

    @Query("SELECT MAX(score) FROM game_records")
    fun getBestScore(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecordEntity)

    @Query("SELECT * FROM game_statistics WHERE id = 1")
    fun getStatistics(): Flow<GameStatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStatistics(stats: GameStatisticsEntity)

    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<GameSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: GameSettingsEntity)

    @Query("SELECT * FROM unlockables")
    fun getAllUnlockables(): Flow<List<UnlockableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnlockable(unlockable: UnlockableEntity)

    @Update
    suspend fun updateUnlockable(unlockable: UnlockableEntity)

    @Query("UPDATE unlockables SET isUnlocked = 1 WHERE hatId = :hatId")
    suspend fun unlockHat(hatId: String)
}
