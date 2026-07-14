package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.JapViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WidgetSimulatorOverlay(
    viewModel: JapViewModel,
    onDismiss: () -> Unit
) {
    val activeMantra by viewModel.activeMantra.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val isHindi = preferences.language == "Hindi"
    val coroutineScope = rememberCoroutineScope()

    // Animation states for floating feedback numbers (+1, +108)
    var floatingText by remember { mutableStateOf<String?>(null) }
    var triggerAnimation by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.96f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "होम स्क्रीन विजेट" else "Home Screen Widget",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (isHindi) "इंटरैक्टिव लाइव प्रीव्यू" else "Interactive Live Preview",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isHindi) "सिम्युलेटर बंद करें" else "Close Simulator",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scrollable container for the Phone Mockup and Android Instructions
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Immersive Phone Mockup containing Launcher Wallpaper & Widget
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(16.dp, RoundedCornerShape(36.dp))
                                .border(4.dp, Color(0xFF332B28), RoundedCornerShape(36.dp))
                                .background(Color(0xFF1E1614), RoundedCornerShape(36.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Phone Screen Surface
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF2E1A12), // Deep sunset spiritual colors
                                                Color(0xFF1A121F),
                                                Color(0xFF0F1524)
                                            )
                                        )
                                    )
                            ) {
                                // Dynamic Floating particles / spiritual dots in wallpaper
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.1f))
                                )

                                // Simulated Android Status Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "12:00",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "5G",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "🔋 100%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                // Centered Simulated App Widget Card
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    // Simulated Widget Container matching widget_container
                                    Card(
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF231B19)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.5.dp,
                                            color = Color(0xFFE65100).copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(8.dp, RoundedCornerShape(24.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(18.dp)
                                        ) {
                                            // Top Row: Logo/App Name & Streak Info
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE65100)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("ॐ", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(
                                                        text = if (isHindi) "नाम जप विजेट" else "Naam Jap Widget",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFE65100),
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }

                                                // Streak Flame
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFFF9800).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isHindi) "${streakInfo.currentStreak} दिन 🔥" else "${streakInfo.currentStreak} Days 🔥",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFFF9800)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // Main Layout: Mantra info and Increment
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Left side: Name and Progress Bar
                                                Column(modifier = Modifier.weight(1.3f)) {
                                                    Text(
                                                        text = activeMantra?.name ?: (if (isHindi) "कोई सक्रिय मंत्र नहीं" else "No Active Mantra"),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        maxLines = 1
                                                    )
                                                    
                                                    val todayTotal = activeMantra?.todayCount ?: 0
                                                    val activeGoal = activeMantra?.dailyGoal ?: 1000
                                                    val progressVal = if (activeGoal > 0) todayTotal.toFloat() / activeGoal.toFloat() else 0f

                                                    Text(
                                                        text = if (isHindi) "आज: ${String.format("%,d", todayTotal)} / ${String.format("%,d", activeGoal)}"
                                                               else "Today: ${String.format("%,d", todayTotal)} / ${String.format("%,d", activeGoal)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFB0A09B),
                                                        fontWeight = FontWeight.Medium
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    LinearProgressIndicator(
                                                        progress = progressVal.coerceIn(0f, 1f),
                                                        color = Color(0xFFE65100),
                                                        trackColor = Color(0xFFE65100).copy(alpha = 0.15f),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(5.dp)
                                                            .clip(CircleShape)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                // Right side: Quick Action "+1 JAP"
                                                Button(
                                                    onClick = {
                                                        viewModel.incrementActiveMantra(1)
                                                        floatingText = if (isHindi) "+1 जप" else "+1 Chant"
                                                        triggerAnimation = true
                                                        coroutineScope.launch {
                                                            delay(700)
                                                            triggerAnimation = false
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFE65100)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .height(38.dp)
                                                        .weight(0.7f)
                                                ) {
                                                    Text(
                                                        text = if (isHindi) "+1 जप" else "+1 JAP",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Animated Overlay Floating Indicator
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = triggerAnimation,
                                    enter = fadeIn() + scaleIn() + slideInVertically(initialOffsetY = { 20 }),
                                    exit = fadeOut() + scaleOut() + slideOutVertically(targetOffsetY = { -40 }),
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE65100), CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = floatingText ?: "+1",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                // Launcher bottom row (Mock dock icons)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("📞", "💬", "🧭", "📸").forEach { icon ->
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(icon, fontSize = 18.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Simulator Dock Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.incrementActiveMantra(108) // 1 Mala
                                        floatingText = if (isHindi) "+108 (माला)" else "+108 (Mala)"
                                        triggerAnimation = true
                                        coroutineScope.launch {
                                            delay(700)
                                            triggerAnimation = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (isHindi) "📿 1 माला जोड़ें" else "📿 Simulate 1 Mala",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // 2. Beautiful Setup Guide
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "इसे अपनी होम स्क्रीन पर कैसे जोड़ें" else "How to add this to your Home Screen",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                val steps = if (isHindi) listOf(
                                    "1. अपने फोन की मुख्य होम स्क्रीन पर जाएं।",
                                    "2. किसी भी खाली जगह पर देर तक दबाए रखें।",
                                    "3. मेनू से 'विजेट' विकल्प पर टैप करें।",
                                    "4. 'नाम जप ट्रैकर' खोजने के लिए नीचे स्क्रॉल करें।",
                                    "5. 'नाम जप विजेट' को खींचकर अपनी स्क्रीन पर रखें।",
                                    "6. ऐप शुरू किए बिना आसानी से ऑफलाइन मंत्र जप करें!"
                                ) else listOf(
                                    "1. Go to your phone's default launcher home screen.",
                                    "2. Long press on any empty space.",
                                    "3. Tap on 'Widgets' from the menu.",
                                    "4. Search or scroll down to find 'Naam Jap Tracker'.",
                                    "5. Drag and place 'Naam Jap Widget' onto your screen.",
                                    "6. Chant offline with ease without launching the app!"
                                )

                                steps.forEach { step ->
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.75f),
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE65100).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (isHindi) "⚡ सिंक सूचना: विजेट आपके सुरक्षित, ऑफलाइन रूम डेटाबेस से सीधे लाइव आंकड़े पढ़ता है, जिससे आप हमेशा प्रेरित रहते हैं!"
                                               else "⚡ Sync Notice: The Widget reads live stats directly from your secure, offline Room database, keeping you always motivated!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFB74D),
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (isHindi) "मंत्र जप जारी रखें" else "Continue Chanting",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
