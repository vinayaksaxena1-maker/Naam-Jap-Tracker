package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JapViewModel
import com.example.ui.Loc
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: JapViewModel = viewModel()
      val preferences by viewModel.preferences.collectAsState()

      MyApplicationTheme(activeTheme = preferences.activeTheme) {
        var showSplash by remember { mutableStateOf(true) }
        var showWidgetSimulator by remember { mutableStateOf(false) }
        var currentTab by remember { mutableStateOf("Home") }
        var currentSubScreen by remember { mutableStateOf<String?>(null) } // "Counter" or "Calendar"

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          if (showSplash) {
            SpiritualSplashScreen(
              onFinished = { showSplash = false }
            )
          } else {
            when (currentSubScreen) {
              "Counter" -> {
                Scaffold(
                  contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                  CounterScreen(
                    viewModel = viewModel,
                    onBackClick = { currentSubScreen = null },
                    modifier = Modifier.padding(innerPadding)
                  )
                }
              }
              "Calendar" -> {
                Scaffold(
                  contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                  CalendarScreen(
                    viewModel = viewModel,
                    onBackClick = { currentSubScreen = null },
                    modifier = Modifier.padding(innerPadding)
                  )
                }
              }
              "Achievements" -> {
                Scaffold(
                  contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                  AchievementsScreen(
                    viewModel = viewModel,
                    onBackClick = { currentSubScreen = null },
                    modifier = Modifier.padding(innerPadding)
                  )
                }
              }
              else -> {
                Scaffold(
                  contentWindowInsets = WindowInsets.safeDrawing,
                  bottomBar = {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                      contentAlignment = Alignment.BottomCenter
                    ) {
                      val density = LocalDensity.current
                      val cutoutRadiusPx = with(density) { 34.dp.toPx() }
                      val cornerRadiusPx = with(density) { 24.dp.toPx() }
                      val controlPointDistancePx = with(density) { 10.dp.toPx() }

                      val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                      val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)

                      // 1. Column for the custom background container
                      Column(
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        // Spacer to match button offset
                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                          modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .drawBehind {
                              val w = size.width
                              val h = size.height
                              val cx = w / 2

                              val path = Path().apply {
                                moveTo(cornerRadiusPx, 0f)

                                val cutoutStart = cx - cutoutRadiusPx - controlPointDistancePx
                                lineTo(cutoutStart, 0f)

                                cubicTo(
                                  x1 = cx - cutoutRadiusPx - (controlPointDistancePx / 2), y1 = 0f,
                                  x2 = cx - cutoutRadiusPx, y2 = cutoutRadiusPx * 0.2f,
                                  x3 = cx - cutoutRadiusPx, y3 = cutoutRadiusPx * 0.55f
                                )

                                cubicTo(
                                  x1 = cx - cutoutRadiusPx, y1 = cutoutRadiusPx * 1.3f,
                                  x2 = cx + cutoutRadiusPx, y2 = cutoutRadiusPx * 1.3f,
                                  x3 = cx + cutoutRadiusPx, y3 = cutoutRadiusPx * 0.55f
                                )

                                val cutoutEnd = cx + cutoutRadiusPx + controlPointDistancePx
                                cubicTo(
                                  x1 = cx + cutoutRadiusPx, y1 = cutoutRadiusPx * 0.2f,
                                  x2 = cx + cutoutRadiusPx + (controlPointDistancePx / 2), y2 = 0f,
                                  x3 = cutoutEnd, y3 = 0f
                                )

                                lineTo(w - cornerRadiusPx, 0f)

                                arcTo(
                                  rect = Rect(w - 2 * cornerRadiusPx, 0f, w, 2 * cornerRadiusPx),
                                  startAngleDegrees = -90f,
                                  sweepAngleDegrees = 90f,
                                  forceMoveTo = false
                                )

                                lineTo(w, h - cornerRadiusPx)

                                arcTo(
                                  rect = Rect(w - 2 * cornerRadiusPx, h - 2 * cornerRadiusPx, w, h),
                                  startAngleDegrees = 0f,
                                  sweepAngleDegrees = 90f,
                                  forceMoveTo = false
                                )

                                lineTo(cornerRadiusPx, h)

                                arcTo(
                                  rect = Rect(0f, h - 2 * cornerRadiusPx, 2 * cornerRadiusPx, h),
                                  startAngleDegrees = 90f,
                                  sweepAngleDegrees = 90f,
                                  forceMoveTo = false
                                )

                                lineTo(0f, cornerRadiusPx)

                                arcTo(
                                  rect = Rect(0f, 0f, 2 * cornerRadiusPx, 2 * cornerRadiusPx),
                                  startAngleDegrees = 180f,
                                  sweepAngleDegrees = 90f,
                                  forceMoveTo = false
                                )

                                close()
                              }

                              drawPath(
                                path = path,
                                color = surfaceColor
                              )

                              drawPath(
                                path = path,
                                color = borderColor,
                                style = Stroke(width = 1.5.dp.toPx())
                              )
                            }
                        ) {
                          Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            // Tab 1: Mantra
                            Column(
                              modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { currentTab = "Mantra" },
                              horizontalAlignment = Alignment.CenterHorizontally,
                              verticalArrangement = Arrangement.Center
                            ) {
                              val isSelected = currentTab == "Mantra"
                              Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Mantra",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                              )
                              Spacer(modifier = Modifier.height(2.dp))
                              if (isSelected) {
                                Box(
                                  modifier = Modifier
                                    .width(14.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                )
                              } else {
                                Text(
                                  text = Loc.get("tabMantra", preferences.language),
                                  style = MaterialTheme.typography.labelSmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                              }
                            }

                            // Tab 2: Profile
                            Column(
                              modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { currentTab = "Profile" },
                              horizontalAlignment = Alignment.CenterHorizontally,
                              verticalArrangement = Arrangement.Center
                            ) {
                              val isSelected = currentTab == "Profile"
                              Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                              )
                              Spacer(modifier = Modifier.height(2.dp))
                              if (isSelected) {
                                Box(
                                  modifier = Modifier
                                    .width(14.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                )
                              } else {
                                Text(
                                  text = Loc.get("tabProfile", preferences.language),
                                  style = MaterialTheme.typography.labelSmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                              }
                            }

                            // Tab 3: Home Space holder to balance the Row
                            Box(
                              modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                              contentAlignment = Alignment.BottomCenter
                            ) {
                              Text(
                                text = Loc.get("tabHome", preferences.language),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentTab == "Home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                fontWeight = if (currentTab == "Home") FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(bottom = 6.dp)
                              )
                            }

                            // Tab 4: Journal
                            Column(
                              modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { currentTab = "Journal" },
                              horizontalAlignment = Alignment.CenterHorizontally,
                              verticalArrangement = Arrangement.Center
                            ) {
                              val isSelected = currentTab == "Journal"
                              Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Journal",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                              )
                              Spacer(modifier = Modifier.height(2.dp))
                              if (isSelected) {
                                Box(
                                  modifier = Modifier
                                    .width(14.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                )
                              } else {
                                Text(
                                  text = Loc.get("tabJournal", preferences.language),
                                  style = MaterialTheme.typography.labelSmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                              }
                            }

                            // Tab 5: Settings
                            Column(
                              modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { currentTab = "Settings" },
                              horizontalAlignment = Alignment.CenterHorizontally,
                              verticalArrangement = Arrangement.Center
                            ) {
                              val isSelected = currentTab == "Settings"
                              Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                              )
                              Spacer(modifier = Modifier.height(2.dp))
                              if (isSelected) {
                                Box(
                                  modifier = Modifier
                                    .width(14.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                )
                              } else {
                                Text(
                                  text = Loc.get("tabSettings", preferences.language),
                                  style = MaterialTheme.typography.labelSmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                              }
                            }
                          }
                        }
                      }

                      // 2. Beautiful Central Circular Floating "Home" Action Button
                      Box(
                        modifier = Modifier
                          .align(Alignment.TopCenter)
                          .size(54.dp)
                          .clip(CircleShape)
                          .background(
                            brush = Brush.linearGradient(
                              colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                              )
                            )
                          )
                          .clickable { currentTab = "Home" },
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Home,
                          contentDescription = "Home",
                          tint = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.size(26.dp)
                        )
                      }
                    }
                  }
                ) { innerPadding ->
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(innerPadding)
                  ) {
                    when (currentTab) {
                      "Home" -> HomeScreen(
                        viewModel = viewModel,
                        onStartJapClick = { currentSubScreen = "Counter" },
                        onAchievementsClick = { currentSubScreen = "Achievements" },
                        onWidgetSimulatorClick = { showWidgetSimulator = true }
                      )
                      "Mantra" -> MantraScreen(
                        viewModel = viewModel
                      )
                      "Profile" -> ProfileScreen(
                        viewModel = viewModel,
                        onCalendarClick = { currentSubScreen = "Calendar" }
                      )
                      "Journal" -> JournalScreen(
                        viewModel = viewModel
                      )
                      "Settings" -> SettingsScreen(
                        viewModel = viewModel
                      )
                    }

                    if (showWidgetSimulator) {
                      WidgetSimulatorOverlay(
                        viewModel = viewModel,
                        onDismiss = { showWidgetSimulator = false }
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
