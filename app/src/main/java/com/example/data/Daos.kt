package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MantraDao {
    @Query("SELECT * FROM mantras ORDER BY createdDate DESC")
    fun getAllMantrasFlow(): Flow<List<Mantra>>

    @Query("SELECT * FROM mantras WHERE isActive = 1 LIMIT 1")
    fun getActiveMantraFlow(): Flow<Mantra?>

    @Query("SELECT * FROM mantras WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveMantraDirect(): Mantra?

    @Query("SELECT * FROM mantras WHERE id = :id LIMIT 1")
    suspend fun getMantraById(id: Int): Mantra?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMantra(mantra: Mantra): Long

    @Update
    suspend fun updateMantra(mantra: Mantra)

    @Delete
    suspend fun deleteMantra(mantra: Mantra)

    @Query("UPDATE mantras SET isActive = 0")
    suspend fun deactivateAllMantras()

    @Transaction
    suspend fun selectActiveMantra(mantraId: Int) {
        deactivateAllMantras()
        val mantra = getMantraById(mantraId)
        if (mantra != null) {
            updateMantra(mantra.copy(isActive = true))
        }
    }
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<History>>

    @Query("SELECT * FROM history WHERE date = :date ORDER BY timestamp DESC")
    fun getHistoryByDateFlow(date: String): Flow<List<History>>

    @Query("SELECT * FROM history WHERE date = :date")
    suspend fun getHistoryByDateDirect(date: String): List<History>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)

    @Delete
    suspend fun deleteHistory(history: History)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM history WHERE mantraId = :mantraId")
    suspend fun deleteHistoryByMantra(mantraId: Int)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<Achievement>)
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal ORDER BY date DESC, timestamp DESC")
    fun getAllJournalFlow(): Flow<List<Journal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal)

    @Delete
    suspend fun deleteJournal(journal: Journal)
}

@Dao
interface AppPreferencesDao {
    @Query("SELECT * FROM app_preferences WHERE id = 1 LIMIT 1")
    fun getPreferencesFlow(): Flow<AppPreferences?>

    @Query("SELECT * FROM app_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferencesDirect(): AppPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: AppPreferences)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

