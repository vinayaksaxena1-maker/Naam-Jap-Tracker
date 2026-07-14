package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class Repository(private val db: AppDatabase) {

    // DAOs
    private val mantraDao = db.mantraDao()
    private val historyDao = db.historyDao()
    private val achievementDao = db.achievementDao()
    private val journalDao = db.journalDao()
    private val preferencesDao = db.appPreferencesDao()

    // Flows
    val allMantras: Flow<List<Mantra>> = mantraDao.getAllMantrasFlow()
    val activeMantra: Flow<Mantra?> = mantraDao.getActiveMantraFlow()
    val allHistory: Flow<List<History>> = historyDao.getAllHistoryFlow()
    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievementsFlow()
    val allJournal: Flow<List<Journal>> = journalDao.getAllJournalFlow()
    val preferences: Flow<AppPreferences?> = preferencesDao.getPreferencesFlow()
    val userProfile: Flow<UserProfile?> = db.userProfileDao().getUserProfileFlow()

    // --- Streak & Progress Calculations (Calculated dynamically) ---
    val streakInfo: Flow<StreakInfo> = allHistory.map { historyList ->
        calculateStreaks(historyList)
    }

    // --- Core Operations ---
    suspend fun saveUserProfile(profile: UserProfile) {
        db.userProfileDao().insertUserProfile(profile)
    }

    suspend fun insertMantra(mantra: Mantra) = mantraDao.insertMantra(mantra)
    
    suspend fun updateMantra(mantra: Mantra) = mantraDao.updateMantra(mantra)

    suspend fun deleteMantra(mantra: Mantra) {
        // First delete history associated with this mantra
        historyDao.deleteHistoryByMantra(mantra.id)
        mantraDao.deleteMantra(mantra)
        // If it was active, activate another one if possible
        val active = mantraDao.getActiveMantraDirect()
        if (active == null) {
            val all = mantraDao.getAllMantrasFlow().firstOrNull() ?: emptyList()
            if (all.isNotEmpty()) {
                mantraDao.selectActiveMantra(all[0].id)
            }
        }
    }

    suspend fun selectActiveMantra(id: Int) = mantraDao.selectActiveMantra(id)

    /**
     * Increments the count of the active mantra.
     * Keeps track of daily counts and lifetime total, updates history, and checks achievements.
     */
    suspend fun incrementActiveMantra(increment: Int = 1) {
        val active = mantraDao.getActiveMantraDirect() ?: return
        val todayStr = getCurrentDateString()
        
        // Update mantra counts
        val updatedTodayCount = active.todayCount + increment
        val updatedTotalCount = active.totalCount + increment
        val updatedMantra = active.copy(
            todayCount = updatedTodayCount,
            totalCount = updatedTotalCount
        )
        mantraDao.updateMantra(updatedMantra)

        // Insert history record
        val historyEntry = History(
            mantraId = active.id,
            mantraName = active.name,
            count = increment,
            date = todayStr,
            timestamp = System.currentTimeMillis()
        )
        historyDao.insertHistory(historyEntry)

        // Re-check and unlock achievements
        checkAndUnlockAchievements(updatedTotalCount)
    }

    /**
     * Undo the last logged history entry (removes it and decrements count of associated mantra)
     */
    suspend fun undoLastSession() {
        val lastHistory = historyDao.getAllHistoryFlow().firstOrNull()?.firstOrNull() ?: return
        
        val associatedMantra = mantraDao.getMantraById(lastHistory.mantraId)
        if (associatedMantra != null) {
            val updatedToday = (associatedMantra.todayCount - lastHistory.count).coerceAtLeast(0)
            val updatedTotal = (associatedMantra.totalCount - lastHistory.count).coerceAtLeast(0)
            mantraDao.updateMantra(associatedMantra.copy(
                todayCount = updatedToday,
                totalCount = updatedTotal
            ))
        }
        
        historyDao.deleteHistory(lastHistory)
    }

    suspend fun deleteHistoryRecord(history: History) {
        val associatedMantra = mantraDao.getMantraById(history.mantraId)
        if (associatedMantra != null) {
            val updatedToday = (associatedMantra.todayCount - history.count).coerceAtLeast(0)
            val updatedTotal = (associatedMantra.totalCount - history.count).coerceAtLeast(0)
            mantraDao.updateMantra(associatedMantra.copy(
                todayCount = updatedToday,
                totalCount = updatedTotal
            ))
        }
        historyDao.deleteHistory(history)
    }

    suspend fun addJournalEntry(journal: Journal) = journalDao.insertJournal(journal)

    suspend fun deleteJournalEntry(journal: Journal) = journalDao.deleteJournal(journal)

    suspend fun updatePreferences(preferences: AppPreferences) = preferencesDao.insertPreferences(preferences)

    suspend fun getPreferencesDirect(): AppPreferences {
        return preferencesDao.getPreferencesDirect() ?: AppPreferences()
    }

    suspend fun resetAllData() {
        db.clearAllTables()
        // Re-initialize default settings and data
        val initialPreferences = AppPreferences(
            id = 1,
            hapticEnabled = true,
            soundEnabled = true,
            keepScreenAwake = true,
            counterSize = "Normal",
            activeTheme = "RamOrange"
        )
        preferencesDao.insertPreferences(initialPreferences)

        val initialMantras = listOf(
            Mantra(name = "Hare Krishna", totalCount = 0, todayCount = 0, dailyGoal = 10000, isActive = false, colorTheme = "KrishnaBlue"),
            Mantra(name = "Om Namah Shivaya", totalCount = 0, todayCount = 0, dailyGoal = 108, isActive = false, colorTheme = "ShivaGrey"),
            Mantra(name = "Shri Ram Jai Ram Jai Jai Ram", totalCount = 0, todayCount = 0, dailyGoal = 10000, isActive = true, colorTheme = "RamOrange"),
            Mantra(name = "Gayatri Mantra", totalCount = 0, todayCount = 0, dailyGoal = 108, isActive = false, colorTheme = "GayatriGold"),
            Mantra(name = "Mahamrityunjaya Mantra", totalCount = 0, todayCount = 0, dailyGoal = 108, isActive = false, colorTheme = "ShivaGrey")
        )
        for (mantra in initialMantras) {
            mantraDao.insertMantra(mantra)
        }

        val initialAchievements = listOf(
            Achievement(name = "First 108", description = "Complete 108 Jap repetitions", target = 108, type = "COUNT"),
            Achievement(name = "1,000 Jap", description = "Reach 1,000 lifetime jap count", target = 1000, type = "COUNT"),
            Achievement(name = "10,000 Jap", description = "Reach 10,000 lifetime jap count", target = 10000, type = "COUNT"),
            Achievement(name = "1 Lakh Jap", description = "Reach 1,00,000 lifetime jap count", target = 100000, type = "COUNT"),
            Achievement(name = "7 Day Streak", description = "Maintain a 7-day jap streak", target = 7, type = "STREAK"),
            Achievement(name = "30 Day Streak", description = "Maintain a 30-day jap streak", target = 30, type = "STREAK"),
            Achievement(name = "108 Day Streak", description = "Maintain a 108-day jap streak", target = 108, type = "STREAK")
        )
        achievementDao.insertAchievements(initialAchievements)
    }

    // --- Backup & Restore ---
    suspend fun exportBackupJson(): String {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val mantrasList = mantraDao.getAllMantrasFlow().firstOrNull() ?: emptyList()
        val historyList = historyDao.getAllHistoryFlow().firstOrNull() ?: emptyList()
        val achievementsList = achievementDao.getAllAchievementsFlow().firstOrNull() ?: emptyList()
        val journalList = journalDao.getAllJournalFlow().firstOrNull() ?: emptyList()
        val preferencesObj = preferencesDao.getPreferencesDirect() ?: AppPreferences()
        val userProfileObj = db.userProfileDao().getUserProfileDirect()

        val backupData = BackupData(
            mantras = mantrasList,
            history = historyList,
            achievements = achievementsList,
            journal = journalList,
            preferences = preferencesObj,
            userProfile = userProfileObj
        )

        val adapter = moshi.adapter(BackupData::class.java)
        return adapter.toJson(backupData)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(BackupData::class.java)
            val backupData = adapter.fromJson(jsonString) ?: return false

            // Clear old tables
            db.clearAllTables()

            // Restore preferences
            preferencesDao.insertPreferences(backupData.preferences)

            // Restore user profile if present
            backupData.userProfile?.let {
                db.userProfileDao().insertUserProfile(it)
            } ?: db.userProfileDao().insertUserProfile(
                UserProfile(
                    id = 1,
                    name = "Sadhak",
                    bio = "Spiritual traveler on the path of Naam Jap.",
                    avatarKey = "lotus",
                    customStatus = "Beginner Seeker"
                )
            )

            // Restore mantras
            for (m in backupData.mantras) {
                mantraDao.insertMantra(m)
            }

            // Restore history
            for (h in backupData.history) {
                historyDao.insertHistory(h)
            }

            // Restore achievements
            achievementDao.insertAchievements(backupData.achievements)

            // Restore journal
            for (j in backupData.journal) {
                journalDao.insertJournal(j)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Helper Calculations ---
    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private suspend fun checkAndUnlockAchievements(lifetimeCount: Int) {
        val achievements = achievementDao.getAllAchievementsFlow().firstOrNull() ?: return
        val streak = calculateStreaks(historyDao.getAllHistoryFlow().firstOrNull() ?: emptyList()).currentStreak

        for (ach in achievements) {
            if (!ach.unlocked) {
                val shouldUnlock = when (ach.type) {
                    "COUNT" -> lifetimeCount >= ach.target
                    "STREAK" -> streak >= ach.target
                    else -> false
                }
                if (shouldUnlock) {
                    achievementDao.updateAchievement(
                        ach.copy(
                            unlocked = true,
                            unlockDate = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun calculateStreaks(historyList: List<History>): StreakInfo {
        if (historyList.isEmpty()) return StreakInfo(0, 0)

        // Group history by date and sum counts
        val dailyCounts = historyList.groupBy { it.date }
            .mapValues { entry -> entry.value.sumOf { it.count } }
            .filter { it.value > 0 }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        val hasToday = dailyCounts.containsKey(todayStr)
        val hasYesterday = dailyCounts.containsKey(yesterdayStr)

        if (!hasToday && !hasYesterday) {
            // No activity today or yesterday, streak is 0
            return StreakInfo(0, calculateLongestStreak(dailyCounts.keys))
        }

        // Calculate current streak
        var currentStreak = 0
        val checkCal = Calendar.getInstance()
        if (!hasToday && hasYesterday) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1) // Start from yesterday
        }

        while (true) {
            val dateStr = sdf.format(checkCal.time)
            if (dailyCounts.containsKey(dateStr)) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        val longestStreak = calculateLongestStreak(dailyCounts.keys)
        return StreakInfo(currentStreak, maxOf(currentStreak, longestStreak))
    }

    private fun calculateLongestStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Parse dates and sort
        val parsedDates = dates.mapNotNull {
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.sorted()

        if (parsedDates.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1

        val cal = Calendar.getInstance()
        for (i in 1 until parsedDates.size) {
            cal.time = parsedDates[i - 1]
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val nextDay = cal.time

            // Compare dates directly at day level
            val calCur = Calendar.getInstance().apply { time = parsedDates[i] }
            val calNext = Calendar.getInstance().apply { time = nextDay }
            
            val isConsecutive = calCur.get(Calendar.YEAR) == calNext.get(Calendar.YEAR) &&
                    calCur.get(Calendar.DAY_OF_YEAR) == calNext.get(Calendar.DAY_OF_YEAR)

            if (isConsecutive) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }
}

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int
)

data class BackupData(
    val mantras: List<Mantra>,
    val history: List<History>,
    val achievements: List<Achievement>,
    val journal: List<Journal>,
    val preferences: AppPreferences,
    val userProfile: UserProfile? = null
)
