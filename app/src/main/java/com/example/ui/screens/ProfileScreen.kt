package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.JapViewModel
import com.example.ui.Loc
import com.example.ui.components.CustomBarChart
import java.text.SimpleDateFormat
import java.util.*

// Avatar options with representative spiritual emojis and background gradient colors
data class AvatarOption(
    val key: String,
    val emoji: String,
    val label: String,
    val colors: List<Color>
)

val avatarOptions = listOf(
    AvatarOption("lotus", "🪷", "Lotus", listOf(Color(0xFFFCE4EC), Color(0xFFF48FB1))),
    AvatarOption("diya", "🪔", "Diya", listOf(Color(0xFFFFF8E1), Color(0xFFFFB300))),
    AvatarOption("om", "ॐ", "Om", listOf(Color(0xFFE1F5FE), Color(0xFF0288D1))),
    AvatarOption("yogi", "🧘", "Yogi", listOf(Color(0xFFE8F5E9), Color(0xFF4CAF50))),
    AvatarOption("temple", "🛕", "Temple", listOf(Color(0xFFEDE7F6), Color(0xFF7E57C2))),
    AvatarOption("mala", "📿", "Mala", listOf(Color(0xFFEFEBE9), Color(0xFF8D6E63))),
    AvatarOption("bell", "🔔", "Bell", listOf(Color(0xFFFFFDE7), Color(0xFFFBC02D))),
    AvatarOption("fire", "🔥", "Yajna", listOf(Color(0xFFFBE9E7), Color(0xFFFF5722)))
)

fun getAvatarByKey(key: String): AvatarOption {
    return avatarOptions.firstOrNull { it.key == key } ?: avatarOptions[0]
}

// Level information calculator based on practice data
data class SpiritualLevelInfo(
    val level: Int,
    val totalXp: Int,
    val xpInCurrentLevel: Int,
    val xpNeededForNext: Int,
    val progress: Float,
    val title: String,
    val badgeColor: Color
)

fun calculateSpiritualLevel(
    totalChants: Int,
    unlockedAchievementsCount: Int,
    currentStreak: Int
): SpiritualLevelInfo {
    // 100 chants = 1 XP
    // 1 unlocked achievement = 25 XP
    // 1 streak day = 10 XP
    val chantsXp = totalChants / 100
    val achievementXp = unlockedAchievementsCount * 25
    val streakXp = currentStreak * 10
    val totalXp = chantsXp + achievementXp + streakXp

    // Every 100 XP is 1 level
    val level = (totalXp / 100).coerceAtLeast(0) + 1
    val xpInCurrentLevel = totalXp % 100
    val xpNeededForNext = 100
    val progress = xpInCurrentLevel.toFloat() / xpNeededForNext.toFloat()

    val title = when (level) {
        1 -> "Arambha Sadhak" // Beginner Seeker
        2 -> "Prarambhik" // Novice Practitioner
        3 -> "Shishya" // Dedicated Disciple
        4 -> "Sadhak" // Steadfast Practitioner
        5 -> "Jap-Karta" // Focused Chanter
        6 -> "Bhakta" // Devoted Soul
        7 -> "Ekagra" // One-Pointed Yogi
        8 -> "Siddha" // Perfected Seeker
        else -> "Acharya" // Chanting Master
    }

    val badgeColor = when (level) {
        1 -> Color(0xFF9E9E9E) // Gray
        2 -> Color(0xFF4CAF50) // Green
        3 -> Color(0xFF03A9F4) // Blue
        4 -> Color(0xFF9C27B0) // Purple
        5 -> Color(0xFFFF9800) // Orange / Gold
        6 -> Color(0xFFE91E63) // Pink
        7 -> Color(0xFFFF5722) // Saffron
        8 -> Color(0xFFFFD700) // Golden Crown
        else -> Color(0xFFFF3D00) // Deep Red Saffron
    }

    return SpiritualLevelInfo(
        level = level,
        totalXp = totalXp,
        xpInCurrentLevel = xpInCurrentLevel,
        xpNeededForNext = xpNeededForNext,
        progress = progress,
        title = title,
        badgeColor = badgeColor
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    viewModel: JapViewModel,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val allHistory by viewModel.allHistory.collectAsState()
    val allAchievements by viewModel.allAchievements.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()

    var activeTab by remember { mutableStateOf("Daily") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalChants = remember(allHistory) { allHistory.sumOf { it.count } }
    val unlockedAchievementsCount = remember(allAchievements) { allAchievements.count { it.unlocked } }
    val currentStreak = streakInfo.currentStreak

    // Level Information
    val levelInfo = remember(totalChants, unlockedAchievementsCount, currentStreak) {
        calculateSpiritualLevel(totalChants, unlockedAchievementsCount, currentStreak)
    }

    // Map history to chart formats based on active tab
    val chartData = remember(allHistory, activeTab) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputSdf = when (activeTab) {
            "Daily" -> SimpleDateFormat("dd E", Locale.getDefault())
            "Weekly" -> SimpleDateFormat("w 'Wk'", Locale.getDefault())
            else -> SimpleDateFormat("MMM", Locale.getDefault())
        }

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

    val averageDaily = if (allHistory.isNotEmpty()) {
        val uniqueDays = allHistory.groupBy { it.date }.size.coerceAtLeast(1)
        totalChants / uniqueDays
    } else 0

    val bestDayCount = if (allHistory.isNotEmpty()) {
        allHistory.groupBy { it.date }.mapValues { it.value.sumOf { s -> s.count } }.maxOf { it.value }
    } else 0

    val totalMala = totalChants / 108
    val uniquePracticedDays = allHistory.groupBy { it.date }.size

    val selectedAvatar = remember(userProfile.avatarKey) { getAvatarByKey(userProfile.avatarKey) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PROFILE HEADER CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar View with Overlapping Level Badge
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clickable { showAvatarDialog = true }
                                .testTag("avatar_container"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Circular Gradient background for Avatar
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = selectedAvatar.colors
                                        )
                                    )
                                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedAvatar.emoji,
                                    fontSize = 42.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Overlapping Circular Level Badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(levelInfo.badgeColor)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = levelInfo.level.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Name, Custom Status and Motive
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { showEditDialog = true },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("edit_profile_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(levelInfo.badgeColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = userProfile.customStatus.ifEmpty { levelInfo.title },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = levelInfo.badgeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = userProfile.bio.ifEmpty { "Devoted Sadhak on the path of chanting." },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Spiritual Level Info & Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (preferences.language == "Hindi") "स्तर ${levelInfo.level}: ${levelInfo.title}" else "Level ${levelInfo.level}: ${levelInfo.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${levelInfo.xpInCurrentLevel}/${levelInfo.xpNeededForNext} XP",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { levelInfo.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = levelInfo.badgeColor,
                        trackColor = levelInfo.badgeColor.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (preferences.language == "Hindi") "अगला स्तर ${levelInfo.xpNeededForNext - levelInfo.xpInCurrentLevel} XP में (मंत्र जप, निरंतरता और उपलब्धियों से XP प्राप्त करें)" else "Next Level in ${levelInfo.xpNeededForNext - levelInfo.xpInCurrentLevel} XP (Gain XP from chanting, maintaining streaks & achievements)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }
        }

        // --- 2. DAILY ACHIEVEMENTS GRID (ROZ KA ACHIEVEMENTS) ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Loc.get("dailyAchievements", preferences.language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (allAchievements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No achievements registered.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allAchievements.take(4).forEach { ach ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (ach.unlocked) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = if (ach.unlocked) {
                                    borderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                } else {
                                    borderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = if (ach.unlocked) "Unlocked" else "Locked",
                                        tint = if (ach.unlocked) {
                                            Color(0xFFFFB300) // Gold Star
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ach.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        color = if (ach.unlocked) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        }
                                    )
                                    Text(
                                        text = if (ach.unlocked) (if (preferences.language == "Hindi") "अनलॉक" else "Unlocked") else (if (preferences.language == "Hindi") "लॉक" else "Locked"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = if (ach.unlocked) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. DETAILED STATS HEADER ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.get("spiritualAnalytics", preferences.language),
                    style = MaterialTheme.typography.titleMedium,
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
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
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
                        text = if (preferences.language == "Hindi") "कुल आवृत्ति ($activeTab)" else "Total Repetitions ($activeTab)",
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
                        Text(
                            text = Loc.get("averageDaily", preferences.language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%,d", averageDaily),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Best day
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Loc.get("bestDay", preferences.language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%,d", bestDayCount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                // Total Mala
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Loc.get("totalMala", preferences.language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%,d", totalMala),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Unique days
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Loc.get("totalDays", preferences.language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (preferences.language == "Hindi") "${uniquePracticedDays} दिन" else "${uniquePracticedDays} days",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // --- 4. DIALOGS ---

    // 1. Edit Profile Dialog
    if (showEditDialog) {
        var tempName by remember { mutableStateOf(userProfile.name) }
        var tempStatus by remember { mutableStateOf(userProfile.customStatus) }
        var tempBio by remember { mutableStateOf(userProfile.bio) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = Loc.get("editSpiritualProfile", preferences.language),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(Loc.get("spiritualName", preferences.language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tempStatus,
                        onValueChange = { tempStatus = it },
                        label = { Text(Loc.get("customStatusLabel", preferences.language)) },
                        placeholder = { Text(Loc.get("customStatusPlaceholder", preferences.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        label = { Text(Loc.get("spiritualBio", preferences.language)) },
                        placeholder = { Text(Loc.get("spiritualBioPlaceholder", preferences.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedProfile = userProfile.copy(
                            name = tempName.trim().ifEmpty { "Sadhak" },
                            customStatus = tempStatus.trim(),
                            bio = tempBio.trim()
                        )
                        viewModel.saveUserProfile(updatedProfile)
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text(Loc.get("save", preferences.language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(Loc.get("cancel", preferences.language))
                }
            }
        )
    }

    // 2. Avatar Selection Dialog
    if (showAvatarDialog) {
        Dialog(onDismissRequest = { showAvatarDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("avatar_picker_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Loc.get("chooseAvatar", preferences.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.height(240.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(avatarOptions) { opt ->
                                val isSelected = opt.key == userProfile.avatarKey
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.saveUserProfile(
                                                userProfile.copy(avatarKey = opt.key)
                                            )
                                            showAvatarDialog = false
                                        }
                                        .padding(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(colors = opt.colors)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = opt.emoji, fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = opt.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showAvatarDialog = false }) {
                        Text(Loc.get("close", preferences.language))
                    }
                }
            }
        }
    }
}

// Small helper for drawing thin borders
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
