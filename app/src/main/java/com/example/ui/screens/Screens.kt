package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.JapViewModel
import com.example.ui.Loc
import com.example.ui.components.CustomBarChart
import com.example.ui.components.DigitalMala
import com.example.ui.components.GoalProgressCircle
import java.text.SimpleDateFormat
import java.util.*

// --- HELPER FUNCTION FOR THEMES ---
@Composable
fun getAvatarForMantra(name: String): String {
    return when {
        name.contains("Krishna", true) -> "🕉️"
        name.contains("Shiva", true) || name.contains("Mahamrityunjaya", true) -> "🔱"
        name.contains("Ram", true) -> "🏹"
        name.contains("Gayatri", true) -> "☀️"
        else -> "📿"
    }
}

// --- OFFLINE SPIRITUAL DETAILS & BENEFIT LOOKUP ---
data class MantraSpiritualDetail(
    val meaning: String,
    val significance: String,
    val translation: String
)

fun getMantraDetails(name: String): MantraSpiritualDetail {
    return when {
        name.contains("Shiva", true) || name.contains("Shiv", true) -> MantraSpiritualDetail(
            meaning = "I bow to the ultimate, divine consciousness within and around me.",
            significance = "Brings deep meditative stillness, clears mental negativity, and fosters cosmic peace.",
            translation = "Om Namah Shivaya: bow to Shiva (Lord of Auspiciousness & Transformation)."
        )
        name.contains("Krishna", true) || name.contains("Hare", true) -> MantraSpiritualDetail(
            meaning = "Oh Divine energy, please engage me in Your loving spiritual service.",
            significance = "Awakens divine ecstasy, pure universal love, and wipes away karmic burdens.",
            translation = "The Hare Krishna Maha Mantra: Chanting the highest vibration of transcendental love."
        )
        name.contains("Ram", true) -> MantraSpiritualDetail(
            meaning = "Victory to the supreme righteousness and divine light dwelling in our hearts.",
            significance = "Instills truth, duty, moral strength, and deep calm in times of distress.",
            translation = "Shree Ram Jai Ram: Rejoice in the embodiment of perfect inner righteousness."
        )
        name.contains("Gayatri", true) -> MantraSpiritualDetail(
            meaning = "We meditate on the divine light of the sun; may it inspire and illuminate our intellect.",
            significance = "Enhances concentration, expands cognitive wisdom, and activates the inner solar channel.",
            translation = "The Gayatri Mantra: The mother of all Vedas, invoking absolute wisdom."
        )
        name.contains("Mani", true) || name.contains("Padme", true) -> MantraSpiritualDetail(
            meaning = "The jewel is in the lotus — compassion and wisdom are unified within the soul.",
            significance = "Cultivates profound empathy, purifies emotional attachments, and links to Avalokiteshvara.",
            translation = "Om Mani Padme Hum: The mantra of universal compassion."
        )
        name.contains("Mahamrityunjaya", true) || name.contains("Mrityunjaya", true) -> MantraSpiritualDetail(
            meaning = "We worship the three-eyed one; free us from the cycle of death and guide us to immortality.",
            significance = "Acts as a healing shield, brings physical well-being, and conquers fears of transition.",
            translation = "Maha Mrityunjaya Mantra: The great death-conquering force of rejuvenation."
        )
        else -> MantraSpiritualDetail(
            meaning = "Chanting this sacred sound connects the individual spirit with the cosmic whole.",
            significance = "Harmonizes breathing patterns, quietens cognitive chatter, and fosters mindfulness.",
            translation = "A customized chanting sound to ground your awareness into the eternal Present."
        )
    }
}

// --- OFFLINE SPIRITUAL PRACTICE RANK STAGES ---
data class RankDetails(
    val title: String,
    val stageName: String, // "Starter", "Pro", "Legend"
    val subtitle: String,
    val minChants: Int,
    val maxChants: Int,
    val color: Color,
    val emoji: String,
    val badgeGradient: List<Color>
)

fun getRankDetails(totalChants: Int): RankDetails {
    return when {
        totalChants < 5000 -> RankDetails(
            title = "Sadhak (Chant Starter)",
            stageName = "Starter",
            subtitle = "Beginning your sacred journey of mindfulness and steady focus.",
            minChants = 0,
            maxChants = 5000,
            color = Color(0xFFD84315), // Bronze/Deep Orange
            emoji = "🌱",
            badgeGradient = listOf(Color(0xFFFFCC80), Color(0xFFFF8A65))
        )
        totalChants < 25000 -> RankDetails(
            title = "Tapasvi (Chant Pro)",
            stageName = "Pro",
            subtitle = "Cultivating disciplined daily sadhana, building dynamic mental resilience.",
            minChants = 5000,
            maxChants = 25000,
            color = Color(0xFF1565C0), // Silver/Blue
            emoji = "⚡",
            badgeGradient = listOf(Color(0xFF90CAF9), Color(0xFF42A5F5))
        )
        else -> RankDetails(
            title = "Rishi (Chant Legend)",
            stageName = "Legend",
            subtitle = "Dwelling in steady transcendental flow, deep cosmic peace, and mastery.",
            minChants = 25000,
            maxChants = 100000, // upper visual bound
            color = Color(0xFF6A1B9A), // Gold/Royal Purple
            emoji = "👑",
            badgeGradient = listOf(Color(0xFFE040FB), Color(0xFFFFAB40))
        )
    }
}


// ==========================================
// 1. HOME SCREEN (DASHBOARD)
// ==========================================
@Composable
fun HomeScreen(
    viewModel: JapViewModel,
    onStartJapClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onWidgetSimulatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMantra by viewModel.activeMantra.collectAsState()
    val allHistory by viewModel.allHistory.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()

    // Calculate Dashboard stats
    val todayTotal = activeMantra?.todayCount ?: 0
    val activeGoal = activeMantra?.dailyGoal ?: 1000
    val progress = if (activeGoal > 0) todayTotal.toFloat() / activeGoal.toFloat() else 0f

    val totalLifetimeCount = allHistory.sumOf { it.count }
    val totalMalaCount = totalLifetimeCount / 108

    val recentActivity = allHistory.take(5)

    // Motivational Spiritual Quotes
    val quotes = listOf(
        "\"Naam Jap is the easiest and most powerful way to connect with the Divine.\"",
        "\"The repeating of the Holy Name dissolves the impurities of the mind and instills peace.\"",
        "\"Mantra chanting calms the nervous system, focuses the intellect, and opens the heart.\"",
        "\"With every single repetition of the Name, light enters your consciousness.\""
    )
    val quoteIndex = remember { mutableIntStateOf(Random().nextInt(quotes.size)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Date header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Naam Jap Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    val sdf = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.getDefault())
                    Text(
                        text = sdf.format(Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Header Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mobile Widget Simulator Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                            .clickable { onWidgetSimulatorClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📱", fontSize = 22.sp)
                    }

                    // Beautiful Achievements trophy button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { onAchievementsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏆", fontSize = 22.sp)
                    }
                }
            }
        }

        // Sadhak Level Status Banner Card
        item {
            val rank = getRankDetails(totalLifetimeCount)
            val progressInStage = if (rank.maxChants > rank.minChants) {
                (totalLifetimeCount - rank.minChants).toFloat() / (rank.maxChants - rank.minChants).toFloat()
            } else {
                1.0f
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = rank.color.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, rank.color.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAchievementsClick() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = rank.badgeGradient
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = rank.emoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sadhana Stage: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = rank.color,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(rank.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rank.stageName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = rank.color,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rank.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = progressInStage.coerceIn(0f, 1f),
                            color = rank.color,
                            trackColor = rank.color.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalLifetimeCount < 25000) {
                                "${String.format("%,d", rank.maxChants - totalLifetimeCount)} chants remaining to next stage"
                            } else {
                                "Supreme Yogi Level Achieved!"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Go to Achievements",
                        tint = rank.color
                    )
                }
            }
        }

        // Today's Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(32.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Daily Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8D6E63)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%,d", todayTotal),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "/ ${String.format("%,d", activeGoal)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFBCAAA4)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Active Mantra Badge styled Box matching design HTML
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "🔥",
                                modifier = Modifier
                                    .background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Active Mantra",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = activeMantra?.name ?: "No active mantra",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .weight(0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        GoalProgressCircle(
                            progress = progress,
                            displayText = "${(progress * 100).toInt()}%",
                            subText = "Goal",
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }
            }
        }

        // Stats Row (Lifetime Counts & Streaks)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lifetime Count
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOTAL JAP", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315).copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%,d", totalLifetimeCount), 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFFD84315)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lifetime chants",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD84315).copy(alpha = 0.6f)
                        )
                    }
                }

                // Lifetime Malas Completed
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEBE9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOTAL MALA", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E342E).copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%,d", totalMalaCount), 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF4E342E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "108 chants each",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4E342E).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Streak
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CURRENT STREAK", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100).copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${streakInfo.currentStreak} Days", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "🔥")
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Consecutive days",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100).copy(alpha = 0.6f)
                        )
                    }
                }

                // Longest Streak
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LONGEST STREAK", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${streakInfo.longestStreak} Days", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "🏆")
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Best achievement",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Start Jap Action Card with a glorious circular TAP button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(32.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = onStartJapClick,
                        modifier = Modifier
                            .size(128.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .padding(8.dp)
                            .testTag("start_jap_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "START JAP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "TAP TO BEGIN YOUR SESSION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D6E63)
                    )
                }
            }
        }

        // Spiritual Quote Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🪔",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = quotes[quoteIndex.intValue],
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Activity Headers & List
        if (recentActivity.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(recentActivity) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = getAvatarForMantra(log.mantraName), fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = log.mantraName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                Text(
                                    text = "${log.date} at ${timeSdf.format(Date(log.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            text = "+${log.count}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📿", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No recorded sessions yet. Tap Start to begin!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. MANTRA MANAGEMENT SCREEN
// ==========================================
@Composable
fun MantraScreen(
    viewModel: JapViewModel,
    modifier: Modifier = Modifier
) {
    val mantras by viewModel.allMantras.collectAsState()
    val activeMantra by viewModel.activeMantra.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var mantraToEdit by remember { mutableStateOf<Mantra?>(null) }

    var newName by remember { mutableStateOf("") }
    var newGoal by remember { mutableStateOf("108") }
    var newTheme by remember { mutableStateOf("RamOrange") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newName = ""
                    newGoal = "108"
                    newTheme = "RamOrange"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_mantra_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Mantra")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Mantras",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Select active mantra to start chanting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(mantras) { mantra ->
                val isActive = activeMantra?.id == mantra.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectActiveMantra(mantra.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = getAvatarForMantra(mantra.name), fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = mantra.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "Today: ${mantra.todayCount}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "Total: ${mantra.totalCount}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "Goal: ${mantra.dailyGoal}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    mantraToEdit = mantra
                                    newName = mantra.name
                                    newGoal = mantra.dailyGoal.toString()
                                    newTheme = mantra.colorTheme
                                    showEditDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Mantra",
                                        tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        if (isActive) {
                            val details = getMantraDetails(mantra.name)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                            ) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Text(
                                    text = "SPIRITUAL TRANSLATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = details.translation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                                Text(
                                    text = "OFFLINE PRACTICE SIGNIFICANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = details.significance,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- ADD DIALOG ---
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Mantra") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Mantra Name") },
                            modifier = Modifier.fillMaxWidth().testTag("add_mantra_name_input")
                        )
                        TextField(
                            value = newGoal,
                            onValueChange = { newGoal = it },
                            label = { Text("Daily Repetitions Goal") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(text = "Visual Theme", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("RamOrange", "KrishnaBlue", "ShivaGrey", "GayatriGold").forEach { theme ->
                                FilterChip(
                                    selected = newTheme == theme,
                                    onClick = { newTheme = theme },
                                    label = { Text(theme.replace("Theme", "")) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.addMantra(
                                    name = newName,
                                    dailyGoal = newGoal.toIntOrNull() ?: 108,
                                    colorTheme = newTheme
                                )
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }

        // --- EDIT DIALOG ---
        if (showEditDialog && mantraToEdit != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Mantra") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Mantra Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = newGoal,
                            onValueChange = { newGoal = it },
                            label = { Text("Daily Repetitions Goal") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(text = "Visual Theme", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("RamOrange", "KrishnaBlue", "ShivaGrey", "GayatriGold").forEach { theme ->
                                FilterChip(
                                    selected = newTheme == theme,
                                    onClick = { newTheme = theme },
                                    label = { Text(theme) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Delete Button
                        Button(
                            onClick = {
                                viewModel.deleteMantra(mantraToEdit!!)
                                showEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Mantra")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank() && mantraToEdit != null) {
                                viewModel.updateMantra(
                                    mantraToEdit!!.copy(
                                        name = newName,
                                        dailyGoal = newGoal.toIntOrNull() ?: 108,
                                        colorTheme = newTheme
                                    )
                                )
                                showEditDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// ==========================================
// 3. STATISTICS SCREEN (CHARTS)
// ==========================================
@Composable
fun StatsScreen(
    viewModel: JapViewModel,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allHistory by viewModel.allHistory.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()

    var activeTab by remember { mutableStateOf("Daily") }

    // Map history to chart formats based on active tabs
    val chartData = remember(allHistory, activeTab) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputSdf = when (activeTab) {
            "Daily" -> SimpleDateFormat("dd E", Locale.getDefault())
            "Weekly" -> SimpleDateFormat("w 'Wk'", Locale.getDefault())
            else -> SimpleDateFormat("MMM", Locale.getDefault())
        }

        // Group history
        val grouped = allHistory.groupBy {
            try {
                val date = sdf.parse(it.date)
                if (date != null) outputSdf.format(date) else it.date
            } catch (e: Exception) {
                it.date
            }
        }.mapValues { entry -> entry.value.sumOf { it.count }.toFloat() }
        
        val list = grouped.entries.map { Pair(it.key, it.value) }.sortedBy { it.first }.takeLast(7)
        if (list.isEmpty()) listOf(Pair("Mon", 0f), Pair("Tue", 0f), Pair("Wed", 0f)) else list
    }

    // Secondary metrics
    val totalJap = allHistory.sumOf { it.count }
    val averageDaily = if (allHistory.isNotEmpty()) {
        val uniqueDays = allHistory.groupBy { it.date }.size.coerceAtLeast(1)
        totalJap / uniqueDays
    } else 0

    val bestDayCount = if (allHistory.isNotEmpty()) {
        allHistory.groupBy { it.date }.mapValues { it.value.sumOf { s -> s.count } }.maxOf { it.value }
    } else 0

    val totalMala = totalJap / 108
    val uniquePracticedDays = allHistory.groupBy { it.date }.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = onCalendarClick,
                    modifier = Modifier.testTag("calendar_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "View Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = "Spiritual growth track logs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Segmented Tab Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(4.dp)
            ) {
                listOf("Daily", "Weekly", "Monthly").forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Custom Bar Chart Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Repetitions ($activeTab)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomBarChart(
                        data = chartData,
                        barColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        // Historical statistics grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Average
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Average Daily", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = String.format("%,d", averageDaily), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Best day
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Best Day", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = String.format("%,d", bestDayCount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Mala
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Total Malas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = String.format("%,d", totalMala), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Days Practiced
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Days Practiced", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$uniquePracticedDays Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CALENDAR SCREEN
// ==========================================
@Composable
fun CalendarScreen(
    viewModel: JapViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allHistory by viewModel.allHistory.collectAsState()
    val activeMantra by viewModel.activeMantra.collectAsState()
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()
    val selectedDateHistory by viewModel.selectedDateHistory.collectAsState()

    // Generate Calendar Days for Current Month
    val calendarDays = remember(allHistory) {
        val days = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        // Go back 25 days, and forward 5 days to make a nice scrollable/navigable range around today
        cal.add(Calendar.DAY_OF_YEAR, -24)
        for (i in 0..29) {
            days.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Calendar History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Calendar Grid
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select a day to view sessions:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(calendarDays.size) { index ->
                            val dateStr = calendarDays[index]
                            
                            // Parse day number
                            val parts = dateStr.split("-")
                            val dayNum = parts.last().toInt().toString()
                            val monthName = try {
                                val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                                if (d != null) SimpleDateFormat("MMM", Locale.getDefault()).format(d) else ""
                            } catch (e: Exception) { "" }

                            // Calculate completions for that day
                            val dayLogs = allHistory.filter { it.date == dateStr }
                            val dayTotal = dayLogs.sumOf { it.count }
                            val dailyGoal = activeMantra?.dailyGoal ?: 1000

                            val isSelected = dateStr == selectedDate

                            val badgeColor = when {
                                dayTotal >= dailyGoal -> Color(0xFF2ECC71) // Green (Completed)
                                dayTotal > 0 -> Color(0xFFF1C40F) // Yellow (Partial)
                                else -> Color(0xFFBDC3C7).copy(alpha = 0.3f) // Grey (No activity)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.selectCalendarDate(dateStr) }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                ) {
                                    Text(
                                        text = dayNum,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayTotal > 0) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = monthName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 8.sp,
                                        color = if (dayTotal > 0) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF2ECC71)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Goal Done", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFF1C40F)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Partial", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFBDC3C7)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("No Activity", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Sessions list for Selected Day
        item {
            Text(
                text = "Sessions on $selectedDate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (selectedDateHistory.isNotEmpty()) {
            items(selectedDateHistory) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = getAvatarForMantra(log.mantraName), fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = log.mantraName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                Text(
                                    text = timeSdf.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${log.count} Jap",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(onClick = { viewModel.deleteHistory(log) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete record",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Naam Jap recorded for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. JOURNAL SCREEN
// ==========================================
@Composable
fun JournalScreen(
    viewModel: JapViewModel,
    modifier: Modifier = Modifier
) {
    val journalEntries by viewModel.allJournal.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val isHindi = preferences.language == "Hindi"

    var showAddDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    noteTitle = ""
                    noteContent = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_journal_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isHindi) "नया नोट" else "New Note"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isHindi) "दैनिक जर्नल" else "Daily Journal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isHindi) "विचार, ध्यान और आध्यात्मिक अनुभव।" else "Reflections, meditations, and realizations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (journalEntries.isNotEmpty()) {
                items(journalEntries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = entry.date,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteJournalEntry(entry) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = if (isHindi) "नोट हटाएं" else "Delete Note",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = entry.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✍️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) "आपका आध्यात्मिक जर्नल खाली है। अपना पहला अनुभव लिखें!" else "Your spiritual journal is empty. Write your first reflection!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(if (isHindi) "नोट / विचार लिखें" else "Write Note / Reflection") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text(if (isHindi) "शीर्षक" else "Title") },
                            modifier = Modifier.fillMaxWidth().testTag("journal_title_input")
                        )
                        TextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text(if (isHindi) "यहाँ अपना अनुभव लिखें..." else "Write reflection here...") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth().testTag("journal_note_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                viewModel.addJournalEntry(
                                    title = noteTitle,
                                    note = noteContent,
                                    date = todayStr
                                )
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text(if (isHindi) "नोट सहेजें" else "Save Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(if (isHindi) "रद्द करें" else "Cancel")
                    }
                }
            )
        }
    }
}

// ==========================================
// 6. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: JapViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val allAchievements by viewModel.allAchievements.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showResetConfirm by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings & Customization",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Preferences, achievements, and offline database utility.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Section: App Language Selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.get("selectLanguage", preferences.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // English
                        val isEn = preferences.language == "English"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isEn) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isEn) 2.dp else 1.dp,
                                    color = if (isEn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updatePreferences(preferences.copy(language = "English"))
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "English 🇬🇧",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isEn) FontWeight.Bold else FontWeight.Normal,
                                color = if (isEn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Hindi
                        val isHi = preferences.language == "Hindi"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isHi) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isHi) 2.dp else 1.dp,
                                    color = if (isHi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updatePreferences(preferences.copy(language = "Hindi"))
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "हिंदी 🇮🇳",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isHi) FontWeight.Bold else FontWeight.Normal,
                                color = if (isHi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section: Sound & Haptics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.get("vibrationAndSounds", preferences.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Haptic Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Loc.get("hapticFeedback", preferences.language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(Loc.get("vibrateOnCounter", preferences.language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = preferences.hapticEnabled,
                            onCheckedChange = {
                                viewModel.updatePreferences(preferences.copy(hapticEnabled = it))
                            },
                            modifier = Modifier.testTag("haptics_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sound Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Loc.get("soundFeedback", preferences.language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(Loc.get("clickBeeps", preferences.language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = preferences.soundEnabled,
                            onCheckedChange = {
                                viewModel.updatePreferences(preferences.copy(soundEnabled = it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Keep Screen Awake Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Loc.get("keepScreenAwake", preferences.language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(Loc.get("preventSleeping", preferences.language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = preferences.keepScreenAwake,
                            onCheckedChange = {
                                viewModel.updatePreferences(preferences.copy(keepScreenAwake = it))
                            }
                        )
                    }
                }
            }
        }

        // Section: Visual Theme Selectors
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.get("visualThemes", preferences.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val themes = listOf(
                        "RamOrange" to "Ram Orange 🏹",
                        "KrishnaBlue" to "Krishna Blue 🕉️",
                        "ShivaGrey" to "Shiva Grey 🔱",
                        "GayatriGold" to "Gayatri Gold ☀️",
                        "Dark" to "Classic Dark 🌙",
                        "AmoledBlack" to "AMOLED Black 🌌"
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(themes.size) { i ->
                            val themeId = themes[i].first
                            val themeLabel = themes[i].second
                            val isSelected = preferences.activeTheme == themeId

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                    .clickable {
                                        viewModel.updatePreferences(preferences.copy(activeTheme = themeId))
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = themeLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Achievements Unlocked
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.get("achievementsTitle", preferences.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    allAchievements.forEach { ach ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (ach.unlocked) Color(0xFFF1C40F).copy(alpha = 0.2f)
                                        else Color.LightGray.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = if (ach.unlocked) "⭐" else "🔒", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ach.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = ach.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (ach.unlocked && ach.unlockDate != null) {
                                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                                Text(
                                    text = sdf.format(Date(ach.unlockDate)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }

        // Section: Database Backup & Restore Utility (JSON-based export/import)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.get("backupAndRestore", preferences.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Loc.get("backupDesc", preferences.language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackup { json ->
                                    if (json != null) {
                                        clipboardManager.setText(AnnotatedString(json))
                                        backupJsonText = json
                                        showBackupDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Export")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Loc.get("exportBackup", preferences.language))
                        }

                        Button(
                            onClick = {
                                importJsonText = ""
                                importStatusMessage = ""
                                showBackupDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Import")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Loc.get("importBackup", preferences.language))
                        }
                    }
                }
            }
        }

        // Section: Danger Zone (Reset)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.get("dangerZone", preferences.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Reset")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Loc.get("resetAll", preferences.language), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Developer Credits (Saltz Labs & Made in India)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Saltz Labs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (preferences.language == "Hindi") "भारत में ❤️ के साथ निर्मित 🇮🇳" else "Made with ❤️ in India 🇮🇳",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.2.4 (Stable Release)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (preferences.language == "Hindi") "नाम जप एक सुरक्षित, १००% ऑफलाइन आध्यात्मिक ट्रैकर है जो आपकी दैनिक साधना, मंत्र जप और शांत ध्यान में सहायता करने के लिए बनाया गया है।" else "Naam Jap is a secure, 100% offline spiritual tracker designed to assist in your daily Sadhana, Mantra Jaap, and quiet mindfulness meditations. Proudly created for seekers everywhere.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // --- RESET CONFIRM DIALOG ---
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(if (preferences.language == "Hindi") "क्या आप ऐप रीसेट करना चाहते हैं?" else "Reset Entire App?") },
            text = { Text(if (preferences.language == "Hindi") "क्या आप वाकई अपने सभी मंत्रों, संचित जप इतिहास और डायरी प्रविष्टियों को हटाना चाहते हैं? यह क्रिया वापस नहीं ली जा सकती!" else "Are you absolutely sure you want to delete all custom mantras, logs, streaks, and journal entries? This operation is completely irreversible!") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (preferences.language == "Hindi") "हां, रीसेट करें" else "Yes, Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text(Loc.get("cancel", preferences.language)) }
            }
        )
    }

    // --- BACKUP CODE EXPORT / IMPORT MODAL ---
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text(if (backupJsonText.isNotEmpty()) (if (preferences.language == "Hindi") "बैकअप कोड कॉपी हुआ!" else "Backup Code Copied!") else (if (preferences.language == "Hindi") "बैकअप कोड आयात करें" else "Import Backup Code")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (backupJsonText.isNotEmpty()) {
                        Text(
                            text = if (preferences.language == "Hindi") "सुरक्षित बैकअप कोड आपके क्लिपबोर्ड पर कॉपी हो गया है। इसे अपने नोट्स में सुरक्षित रखें या दूसरे डिवाइस के साथ साझा करें:" else "The secure backup code has been copied to your clipboard. Save it in your notes or share it with another device:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextField(
                            value = backupJsonText,
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = if (preferences.language == "Hindi") "अपनी प्रगति को पुनर्स्थापित करने के लिए यहां अपना बैकअप कोड पेस्ट करें:" else "Paste your previously copied backup code here to fully restore your progress:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            placeholder = { Text(if (preferences.language == "Hindi") "यहाँ कोड पेस्ट करें..." else "Paste code here...") },
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (importStatusMessage.isNotEmpty()) {
                            val isSuccess = importStatusMessage.contains("Success", true) || importStatusMessage.contains("सफलता", true)
                            Text(
                                text = if (importStatusMessage.contains("Success", true)) (if (preferences.language == "Hindi") "सफलता! बैकअप पुनर्स्थापित हुआ।" else "Success! Backup restored.") else (if (preferences.language == "Hindi") "त्रुटि: अमान्य बैकअप कोड।" else "Error: Invalid backup format."),
                                color = if (isSuccess) Color(0xFF2ECC71) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (backupJsonText.isNotEmpty()) {
                            backupJsonText = ""
                            showBackupDialog = false
                        } else {
                            if (importJsonText.isNotBlank()) {
                                viewModel.importBackup(importJsonText) { success ->
                                    if (success) {
                                        importStatusMessage = "Success! Backup restored."
                                        importJsonText = ""
                                        showBackupDialog = false
                                    } else {
                                        importStatusMessage = "Error: Invalid backup format."
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(if (backupJsonText.isNotEmpty()) "OK" else (if (preferences.language == "Hindi") "पुनर्स्थापित करें" else "Restore"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        backupJsonText = ""
                        showBackupDialog = false
                    }
                ) {
                    Text(Loc.get("close", preferences.language))
                }
            }
        )
    }
}

// ==========================================
// 7. INTERACTIVE JAP COUNTER & MALA MODE SCREEN
// ==========================================
@Composable
fun CounterScreen(
    viewModel: JapViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMantra by viewModel.activeMantra.collectAsState()
    val preferences by viewModel.preferences.collectAsState()

    // Keep screen awake if enabled in preferences
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(preferences.keepScreenAwake) {
        if (preferences.keepScreenAwake) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = false
        }
    }

    var activeMode by remember { mutableStateOf("Tap Counter") } // "Tap Counter" vs "Digital Mala"
    var showResetConfirm by remember { mutableStateOf(false) }

    // Keep track of the current session beads completed (from 0 to 107)
    var currentBeadSession by remember { mutableIntStateOf(0) }

    // Session analytics states (offline timer & CPM)
    var sessionStartMillis by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var sessionTaps by remember { mutableIntStateOf(0) }

    LaunchedEffect(sessionStartMillis) {
        if (sessionStartMillis > 0L) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                elapsedSeconds = (System.currentTimeMillis() - sessionStartMillis) / 1000
            }
        }
    }

    val registerTap: () -> Unit = {
        if (sessionStartMillis == 0L) {
            sessionStartMillis = System.currentTimeMillis()
        }
        sessionTaps++
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeStr = String.format("%02d:%02d", minutes, seconds)
    
    val cpm = if (elapsedSeconds > 0) {
        (sessionTaps.toFloat() / elapsedSeconds.toFloat() * 60f).toInt()
    } else {
        0
    }

    val mantraName = activeMantra?.name ?: "No Active Mantra"
    val totalCount = activeMantra?.totalCount ?: 0
    val dailyGoal = activeMantra?.dailyGoal ?: 1000

    // Complete Mala calculations
    val currentMala = (totalCount / 108) + 1
    val currentBead = totalCount % 108

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = mantraName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* Settings context option */ }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Options", tint = Color.Transparent)
            }
        }

        // Mode Segmented Toggles (Tap Counter vs Mala Mode)
        Row(
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(4.dp)
        ) {
            listOf("Tap Counter", "Mala Mode").forEach { mode ->
                val isSelected = activeMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeMode = mode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Real-Time Offline Chanting Session Bar
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Session Timer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Timer: $timeStr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Chanting Speed",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Speed: $cpm CPM",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Center Content depending on active mode
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            contentAlignment = Alignment.Center
        ) {
            if (activeMode == "Tap Counter") {
                // Large circular counter layout
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                        .clickable {
                            registerTap()
                            viewModel.incrementActiveMantra(1)
                            val newSessionBead = currentBeadSession + 1
                            if (newSessionBead >= 108) {
                                viewModel.triggerBellSound()
                                currentBeadSession = 0
                            } else {
                                currentBeadSession = newSessionBead
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Outer border
                    GoalProgressCircle(
                        progress = (currentBead.toFloat() / 108f),
                        displayText = String.format("%,d", totalCount),
                        subText = "Total Jap",
                        primaryColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(230.dp)
                    )
                }
            } else {
                // Beautiful interactive Canvas digital mala
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    DigitalMala(
                        totalBeads = 54, // 54 bead half mala rotates beautifully
                        currentBead = currentBead,
                        onBeadClick = {
                            registerTap()
                            viewModel.incrementActiveMantra(1)
                            val newSessionBead = currentBeadSession + 1
                            if (newSessionBead >= 108) {
                                viewModel.triggerBellSound()
                                currentBeadSession = 0
                            } else {
                                currentBeadSession = newSessionBead
                            }
                        },
                        modifier = Modifier
                            .size(280.dp)
                            .testTag("digital_mala_canvas")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🌸 Complete 108 beads for 1 Mala 🌸",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Secondary counters
        Card(
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Mala", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Text("$currentMala", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bead", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Text("$currentBead / 108", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Daily Goal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Text(String.format("%,d", dailyGoal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Interactive control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo last click
            IconButton(
                onClick = { viewModel.undoLastSession() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                enabled = totalCount > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo Click",
                    tint = if (totalCount > 0) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }

            // Big Tap click button (+1)
            Button(
                onClick = {
                    registerTap()
                    viewModel.incrementActiveMantra(1)
                    val newSessionBead = currentBeadSession + 1
                    if (newSessionBead >= 108) {
                        viewModel.triggerBellSound()
                        currentBeadSession = 0
                    } else {
                        currentBeadSession = newSessionBead
                    }
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(80.dp)
                    .testTag("increment_count_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("+1", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }

            // Reset
            IconButton(
                onClick = { showResetConfirm = true },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                enabled = totalCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = if (totalCount > 0) MaterialTheme.colorScheme.error else Color.LightGray
                )
            }
        }
    }

    // --- RESET CONFIRMATION FOR MANTRA COUNT ---
    if (showResetConfirm && activeMantra != null) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Count?") },
            text = { Text("Are you sure you want to reset today and total counts for \"${activeMantra!!.name}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMantra(activeMantra!!.copy(todayCount = 0, totalCount = 0))
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}


// ==========================================
// 6. ACHIEVEMENTS & SADHAK STAGES SCREEN
// ==========================================
// --- EXTENDED ACHIEVEMENT META INFO ---
data class AchievementExtendedInfo(
    val emoji: String,
    val spiritualBenefit: String,
    val healthBenefit: String,
    val practiceTip: String
)

fun getAchievementExtendedInfo(name: String): AchievementExtendedInfo {
    return when (name) {
        "First 108" -> AchievementExtendedInfo(
            emoji = "🪔",
            spiritualBenefit = "Completing one full Mala (108 repetitions) aligns your vital breath (Prana) with the cosmic rhythm.",
            healthBenefit = "Triggers the relaxation response, lowers blood pressure, and calms the parasympathetic nervous system.",
            practiceTip = "Try to coordinate your chanting rhythm with slow, steady nasal breathing."
        )
        "1,000 Jap" -> AchievementExtendedInfo(
            emoji = "🌸",
            spiritualBenefit = "A thousand repetitions purifies the energy fields (aura) around you, fostering inner mental clarity.",
            healthBenefit = "Increases alpha brain waves, reducing daily stress, fatigue, and cognitive load.",
            practiceTip = "Chant early in the morning (Brahma Muhurta) for maximum silence and mental focus."
        )
        "10,000 Jap" -> AchievementExtendedInfo(
            emoji = "⚡",
            spiritualBenefit = "Reaching 10k begins the process of deep subconscious cleansing, writing positive neural circuits.",
            healthBenefit = "Significantly increases heart rate variability (HRV) and emotional resilience against anxiety.",
            practiceTip = "Maintain a steady pace. Consistency is far more powerful than rushing the counts."
        )
        "1 Lakh Jap" -> AchievementExtendedInfo(
            emoji = "👑",
            spiritualBenefit = "An extraordinary spiritual milestone. Chanting 100,000 times integrates the mantra into your heartbeat.",
            healthBenefit = "Promotes neuroplasticity, thickens the prefrontal cortex, and enhances deep emotional regulation.",
            practiceTip = "Celebrate this achievement with silent introspection, gratitude, and a peaceful day of reading."
        )
        "7 Day Streak" -> AchievementExtendedInfo(
            emoji = "📅",
            spiritualBenefit = "A full week of chanting establishes a daily spiritual anchor (Sadhana) in your material life.",
            healthBenefit = "Helps in habit formation, establishing strong synaptic pathways for mental focus.",
            practiceTip = "Set a specific daily alarm or calendar block to protect your sacred 10 minutes of Jap."
        )
        "30 Day Streak" -> AchievementExtendedInfo(
            emoji = "🔥",
            spiritualBenefit = "A month-long discipline lights the inner flame of devotion and burns deep-rooted distractions (Vrittis).",
            healthBenefit = "Promotes deep stress recovery and builds unparalleled self-discipline and willpower.",
            practiceTip = "If busy, chant even just one Mala (108) to keep your streak alive. Never break the chain!"
        )
        "108 Day Streak" -> AchievementExtendedInfo(
            emoji = "🌌",
            spiritualBenefit = "Chanting for 108 consecutive days brings complete alignment of the body's major energy channels (Chakras).",
            healthBenefit = "Induces a permanent state of lower stress activation and deep neural peace.",
            practiceTip = "You are a master of habit. Inspire someone else to start their offline mindfulness journey today."
        )
        else -> AchievementExtendedInfo(
            emoji = "⭐",
            spiritualBenefit = "Chanting unites personal consciousness with the infinite universal vibration.",
            healthBenefit = "Improves vocal cord strength, lung capacity, and mental clarity.",
            practiceTip = "Continue practicing with love, devotion, and perfect awareness."
        )
    }
}

@Composable
fun AchievementsScreen(
    viewModel: JapViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allAchievements by viewModel.allAchievements.collectAsState()
    val allHistory by viewModel.allHistory.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()

    val totalLifetimeCount = allHistory.sumOf { it.count }
    val rank = getRankDetails(totalLifetimeCount)

    var selectedStageTab by remember { mutableStateOf("Starter") } // "Starter", "Pro", "Legend"
    var selectedAchievementForDetail by remember { mutableStateOf<com.example.data.Achievement?>(null) }

    // Grouping criteria matching the Room achievements
    val filteredAchievements = remember(allAchievements, selectedStageTab) {
        when (selectedStageTab) {
            "Starter" -> allAchievements.filter { it.target <= 1000 && it.type == "COUNT" }
            "Pro" -> allAchievements.filter { (it.target > 1000 && it.target <= 10000 && it.type == "COUNT") || (it.type == "STREAK" && it.target <= 30) }
            "Legend" -> allAchievements.filter { it.target > 10000 || (it.type == "STREAK" && it.target > 30) }
            else -> allAchievements
        }
    }

    val unlockedCount = allAchievements.count { it.unlocked }
    val totalCount = allAchievements.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Sadhana Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Progress pill
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$unlockedCount/$totalCount Unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- ACTIVE STATUS CARD ---
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = rank.badgeGradient
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = rank.emoji, fontSize = 36.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .background(rank.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "STAGE: ${rank.stageName.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = rank.color,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = rank.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = rank.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Lifetime Chants",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = String.format("%,d chants", totalLifetimeCount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = rank.color
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Current Streak",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${streakInfo.currentStreak} Days 🔥",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }

            // --- STAGE SELECTION TABS ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "Starter" to "🌱 Starter",
                        "Pro" to "⚡ Pro",
                        "Legend" to "👑 Legend"
                    )
                    tabs.forEach { (key, label) ->
                        val isSelected = selectedStageTab == key
                        val activeColor = when (key) {
                            "Starter" -> Color(0xFFD84315)
                            "Pro" -> Color(0xFF1565C0)
                            else -> Color(0xFF6A1B9A)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedStageTab = key }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- ACHIEVEMENT CARDS LIST ---
            if (filteredAchievements.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🧘", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No achievements found in this stage yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredAchievements) { ach ->
                    val ext = getAchievementExtendedInfo(ach.name)
                    val activeColor = when (selectedStageTab) {
                        "Starter" -> Color(0xFFD84315)
                        "Pro" -> Color(0xFF1565C0)
                        else -> Color(0xFF6A1B9A)
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAchievementForDetail = ach },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ach.unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (ach.unlocked) 1.5.dp else 1.dp,
                            color = if (ach.unlocked) activeColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (ach.unlocked) 2.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge Icon / Lock Indicator
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (ach.unlocked) activeColor.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (ach.unlocked) ext.emoji else "🔒",
                                    fontSize = if (ach.unlocked) 26.sp else 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ach.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ach.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (ach.unlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )

                                if (ach.unlocked && ach.unlockDate != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ach.unlockDate))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "✨ Unlocked on $formattedDate",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = activeColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "View Details",
                                tint = if (ach.unlocked) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DETAILED ACHIEVEMENT BENEFITS DIALOG ---
    selectedAchievementForDetail?.let { ach ->
        val ext = getAchievementExtendedInfo(ach.name)
        val activeColor = when {
            ach.name == "First 108" || ach.name == "1,000 Jap" -> Color(0xFFD84315)
            ach.name == "7 Day Streak" || ach.name == "30 Day Streak" || ach.name == "10,000 Jap" -> Color(0xFF1565C0)
            else -> Color(0xFF6A1B9A)
        }
        AlertDialog(
            onDismissRequest = { selectedAchievementForDetail = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (ach.unlocked) ext.emoji else "🔒", fontSize = 32.sp)
                }
            },
            title = {
                Text(
                    text = ach.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = if (ach.unlocked) "🎉 Unlocked Milestone!" else "🔒 Target Milestone",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (ach.unlocked) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    Text(
                        text = "Vedic Spiritual Significance".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = ext.spiritualBenefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Mindfulness & Health Benefits".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = ext.healthBenefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Practical Sadhana Tip".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = ext.practiceTip,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedAchievementForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = activeColor)
                ) {
                    Text("Dhanyawad (Close)")
                }
            }
        )
    }
}
