package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class JapViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = Repository(db)
    private val mantraService: MantraService = RoomMantraService(db, repository)

    // Flows from MantraService & Repository
    val allMantras: StateFlow<List<Mantra>> = mantraService.allMantras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMantra: StateFlow<Mantra?> = mantraService.activeMantra
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allHistory: StateFlow<List<History>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAchievements: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJournal: StateFlow<List<Journal>> = repository.allJournal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<AppPreferences> = repository.preferences
        .map { it ?: AppPreferences() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val streakInfo: StateFlow<StreakInfo> = repository.streakInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakInfo(0, 0))

    // UI Local State for Calendar selection
    private val _selectedCalendarDate = MutableStateFlow(getCurrentDateString())
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    val selectedDateHistory: StateFlow<List<History>> = combine(
        allHistory,
        _selectedCalendarDate
    ) { historyList, selectedDate ->
        historyList.filter { it.date == selectedDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tone generator for audio feedback (bell sound on completion)
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Core Database operations using MantraService ---
    
    fun selectActiveMantra(mantraId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mantraService.selectActiveMantra(mantraId)
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile)
        }
    }

    fun addMantra(name: String, dailyGoal: Int, colorTheme: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mantraService.addMantra(name, dailyGoal, colorTheme)
        }
    }

    fun updateMantra(mantra: Mantra) {
        viewModelScope.launch(Dispatchers.IO) {
            mantraService.updateMantra(mantra)
        }
    }

    fun deleteMantra(mantra: Mantra) {
        viewModelScope.launch(Dispatchers.IO) {
            mantraService.deleteMantra(mantra)
        }
    }

    fun incrementActiveMantra(amount: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementActiveMantra(amount)
            
            // Check haptics & sounds
            val prefs = repository.getPreferencesDirect()
            if (prefs.hapticEnabled) {
                triggerHapticFeedback()
            }
            if (prefs.soundEnabled) {
                triggerBeep()
            }
        }
    }

    fun undoLastSession() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.undoLastSession()
        }
    }

    fun deleteHistory(history: History) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistoryRecord(history)
        }
    }

    fun addJournalEntry(title: String, note: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val journal = Journal(
                title = title,
                note = note,
                date = date
            )
            repository.addJournalEntry(journal)
        }
    }

    fun deleteJournalEntry(journal: Journal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteJournalEntry(journal)
        }
    }

    fun updatePreferences(pref: AppPreferences) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePreferences(pref)
        }
    }

    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAllData()
        }
    }

    fun selectCalendarDate(dateStr: String) {
        _selectedCalendarDate.value = dateStr
    }

    // --- Sound and Haptic Trigger Utilities ---

    private fun triggerHapticFeedback() {
        val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }

    private fun triggerBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerBellSound() {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.getPreferencesDirect().soundEnabled) {
                try {
                    // Play a long double-beep to simulate a bell
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (repository.getPreferencesDirect().hapticEnabled) {
                // Strong double vibration pattern
                val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 150), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(250)
                    }
                }
            }
        }
    }

    // --- Backup & Restore ---

    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = try {
                repository.exportBackupJson()
            } catch (e: Exception) {
                null
            }
            onResult(json)
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.importBackupJson(jsonString)
            onResult(success)
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator?.release()
    }
}
