package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Service contract for handling all offline-first CRUD operations on Mantras
 * and managing the active mantra selection.
 *
 * This provides a clean architectural boundary, abstracting data persistence
 * from the presentation layer (ViewModels). It leverages Room Database to ensure
 * high-performance, reactive, and 100% local/offline-first data persistence.
 */
interface MantraService {
    /**
     * Observable stream of all mantras in the database, ordered by creation date.
     */
    val allMantras: Flow<List<Mantra>>

    /**
     * Observable stream of the currently selected active mantra, or null if none is selected.
     */
    val activeMantra: Flow<Mantra?>

    /**
     * Retrieves a single mantra by its unique identifier.
     */
    suspend fun getMantraById(id: Int): Mantra?

    /**
     * Inserts a new mantra with a specified name, daily target goal, and visual color theme.
     * @return The auto-generated row ID of the newly added mantra.
     */
    suspend fun addMantra(name: String, dailyGoal: Int, colorTheme: String): Long

    /**
     * Updates an existing mantra's attributes (e.g., name, daily goal, colors, or counts).
     */
    suspend fun updateMantra(mantra: Mantra)

    /**
     * Deletes a mantra from the database. Also handles associated history records 
     * and automatically re-routes active mantra state if the deleted mantra was active.
     */
    suspend fun deleteMantra(mantra: Mantra)

    /**
     * Set the selected mantra as the single active mantra, deactivating all other mantras.
     */
    suspend fun selectActiveMantra(id: Int)
}

/**
 * Room Database implementation of the [MantraService], implementing local, offline-first storage.
 */
class RoomMantraService(
    private val db: AppDatabase,
    private val repository: Repository
) : MantraService {

    private val mantraDao = db.mantraDao()

    override val allMantras: Flow<List<Mantra>> = repository.allMantras
    override val activeMantra: Flow<Mantra?> = repository.activeMantra

    override suspend fun getMantraById(id: Int): Mantra? {
        return mantraDao.getMantraById(id)
    }

    override suspend fun addMantra(name: String, dailyGoal: Int, colorTheme: String): Long {
        val mantra = Mantra(
            name = name,
            dailyGoal = dailyGoal,
            colorTheme = colorTheme,
            isActive = false
        )
        return mantraDao.insertMantra(mantra)
    }

    override suspend fun updateMantra(mantra: Mantra) {
        repository.updateMantra(mantra)
    }

    override suspend fun deleteMantra(mantra: Mantra) {
        repository.deleteMantra(mantra)
    }

    override suspend fun selectActiveMantra(id: Int) {
        repository.selectActiveMantra(id)
    }
}
