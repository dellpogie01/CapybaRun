package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GameRecordEntity::class,
        GameStatisticsEntity::class,
        GameSettingsEntity::class,
        UnlockableEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CapybaDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: CapybaDatabase? = null

        fun getDatabase(context: Context): CapybaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CapybaDatabase::class.java,
                    "capybarun_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.gameDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: GameDao) {
                // Initialize default stats and settings
                dao.insertOrUpdateStatistics(GameStatisticsEntity(id = 1))
                dao.insertOrUpdateSettings(GameSettingsEntity(id = 1))

                // Initialize unlockable hats
                val defaultHats = listOf(
                    UnlockableEntity("none", "Default", "No hat, just pure capybara charm", true, 0),
                    UnlockableEntity("explorer", "Explorer Hat", "A trusty traveler hat for brave runs", false, 5),
                    UnlockableEntity("crown", "Golden Crown", "Fit for the ultimate running royalty", false, 500),
                    UnlockableEntity("flower", "Flower Crown", "Fresh daisies for a sunny jog", false, 15),
                    UnlockableEntity("wizard", "Wizard Cap", "Magical flight mastery unlocked", false, 30)
                )
                defaultHats.forEach { dao.insertUnlockable(it) }
            }
        }
    }
}
