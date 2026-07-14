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
        Mantra::class,
        History::class,
        Achievement::class,
        Journal::class,
        AppPreferences::class,
        UserProfile::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mantraDao(): MantraDao
    abstract fun historyDao(): HistoryDao
    abstract fun achievementDao(): AchievementDao
    abstract fun journalDao(): JournalDao
    abstract fun appPreferencesDao(): AppPreferencesDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "naam_jap_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Default User Profile
            db.userProfileDao().insertUserProfile(
                UserProfile(
                    id = 1,
                    name = "Sadhak",
                    bio = "Spiritual traveler on the path of Naam Jap.",
                    avatarKey = "lotus",
                    customStatus = "Beginner Seeker"
                )
            )

            // Default Preferences
            db.appPreferencesDao().insertPreferences(
                AppPreferences(
                    id = 1,
                    hapticEnabled = true,
                    soundEnabled = true,
                    keepScreenAwake = true,
                    counterSize = "Normal",
                    activeTheme = "RamOrange"
                )
            )

            // Default Mantras
            val initialMantras = listOf(
                Mantra(
                    name = "Hare Krishna",
                    totalCount = 0,
                    todayCount = 0,
                    dailyGoal = 10000,
                    isActive = false,
                    colorTheme = "KrishnaBlue"
                ),
                Mantra(
                    name = "Om Namah Shivaya",
                    totalCount = 0,
                    todayCount = 0,
                    dailyGoal = 108,
                    isActive = false,
                    colorTheme = "ShivaGrey"
                ),
                Mantra(
                    name = "Shri Ram Jai Ram Jai Jai Ram",
                    totalCount = 0,
                    todayCount = 0,
                    dailyGoal = 10000,
                    isActive = true, // Ram Orange is active by default
                    colorTheme = "RamOrange"
                ),
                Mantra(
                    name = "Gayatri Mantra",
                    totalCount = 0,
                    todayCount = 0,
                    dailyGoal = 108,
                    isActive = false,
                    colorTheme = "GayatriGold"
                ),
                Mantra(
                    name = "Mahamrityunjaya Mantra",
                    totalCount = 0,
                    todayCount = 0,
                    dailyGoal = 108,
                    isActive = false,
                    colorTheme = "ShivaGrey"
                )
            )
            for (mantra in initialMantras) {
                db.mantraDao().insertMantra(mantra)
            }

            // Default Achievements
            val initialAchievements = listOf(
                Achievement(name = "First 108", description = "Complete 108 Jap repetitions", target = 108, type = "COUNT"),
                Achievement(name = "1,000 Jap", description = "Reach 1,000 lifetime jap count", target = 1000, type = "COUNT"),
                Achievement(name = "10,000 Jap", description = "Reach 10,000 lifetime jap count", target = 10000, type = "COUNT"),
                Achievement(name = "1 Lakh Jap", description = "Reach 1,00,000 lifetime jap count", target = 100000, type = "COUNT"),
                Achievement(name = "7 Day Streak", description = "Maintain a 7-day jap streak", target = 7, type = "STREAK"),
                Achievement(name = "30 Day Streak", description = "Maintain a 30-day jap streak", target = 30, type = "STREAK"),
                Achievement(name = "108 Day Streak", description = "Maintain a 108-day jap streak", target = 108, type = "STREAK")
            )
            db.achievementDao().insertAchievements(initialAchievements)
        }
    }
}
