package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.History
import com.example.data.Mantra
import com.example.data.Repository
import com.example.data.StreakInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NaamJapWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_INCREMENT) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext, this)
                    val mantraDao = db.mantraDao()
                    val historyDao = db.historyDao()
                    
                    val active = mantraDao.getActiveMantraDirect()
                    if (active != null) {
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val updatedToday = active.todayCount + 1
                        val updatedTotal = active.totalCount + 1
                        val updatedMantra = active.copy(todayCount = updatedToday, totalCount = updatedTotal)
                        mantraDao.updateMantra(updatedMantra)

                        val historyEntry = History(
                            mantraId = active.id,
                            mantraName = active.name,
                            count = 1,
                            date = todayStr,
                            timestamp = System.currentTimeMillis()
                        )
                        historyDao.insertHistory(historyEntry)

                        // Trigger widget UI update across all active instances
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val thisWidget = ComponentName(context, NaamJapWidgetProvider::class.java)
                        val allIds = appWidgetManager.getAppWidgetIds(thisWidget)
                        allIds.forEach { id ->
                            updateAppWidget(context, appWidgetManager, id)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_INCREMENT = "com.example.widget.ACTION_INCREMENT"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.naam_jap_widget)

            // Setup PendingIntent to launch MainActivity when clicking the card
            val appIntent = Intent(context, MainActivity::class.java)
            val appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent)

            // Setup PendingIntent for +1 button
            val incrementIntent = Intent(context, NaamJapWidgetProvider::class.java).apply {
                action = ACTION_INCREMENT
            }
            val incrementPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId, // unique RequestCode per widget instance
                incrementIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_increment, incrementPendingIntent)

            // Query active database state in IO Thread
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext, this)
                    val activeMantra = db.mantraDao().getActiveMantraDirect()
                    val historyList = db.historyDao().getAllHistoryFlow().firstOrNull() ?: emptyList()
                    val streak = calculateStreaks(historyList)
                    val prefs = db.appPreferencesDao().getPreferencesDirect()
                    val isHindi = prefs?.language == "Hindi"

                    if (activeMantra != null) {
                        views.setTextViewText(R.id.widget_mantra_name, activeMantra.name)
                        views.setTextViewText(
                            R.id.widget_progress_text,
                            if (isHindi) "आज: ${String.format("%,d", activeMantra.todayCount)} / ${String.format("%,d", activeMantra.dailyGoal)}"
                            else "Today: ${String.format("%,d", activeMantra.todayCount)} / ${String.format("%,d", activeMantra.dailyGoal)}"
                        )
                        val progressPercent = if (activeMantra.dailyGoal > 0) {
                            (activeMantra.todayCount.toFloat() / activeMantra.dailyGoal.toFloat() * 100).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }
                        views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
                    } else {
                        views.setTextViewText(R.id.widget_mantra_name, if (isHindi) "कोई सक्रिय मंत्र नहीं" else "No Active Mantra")
                        views.setTextViewText(R.id.widget_progress_text, if (isHindi) "ऐप में एक मंत्र चुनें" else "Choose a mantra in app")
                        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
                    }

                    views.setTextViewText(R.id.widget_streak, if (isHindi) "${streak.currentStreak} दिन 🔥" else "${streak.currentStreak} Days 🔥")

                    // Notify AppWidgetManager of update
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun calculateStreaks(historyList: List<History>): StreakInfo {
            if (historyList.isEmpty()) return StreakInfo(0, 0)
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
                return StreakInfo(0, 0)
            }

            var currentStreak = 0
            val checkCal = Calendar.getInstance()
            if (!hasToday && hasYesterday) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
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
            return StreakInfo(currentStreak, currentStreak)
        }
    }
}
