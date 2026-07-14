package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RamOrangeColorScheme = lightColorScheme(
    primary = RamOrangePrimary,
    background = RamOrangeBackground,
    surface = RamOrangeSurface,
    onPrimary = Color.White,
    onBackground = RamOrangeOnBackground,
    onSurface = RamOrangeOnSurface,
    secondaryContainer = RamOrangeAccent,
    onSecondaryContainer = RamOrangeOnBackground,
    outline = RamOrangeOutline,
    outlineVariant = RamOrangeOutline
)

private val KrishnaBlueColorScheme = lightColorScheme(
    primary = KrishnaBluePrimary,
    background = KrishnaBlueBackground,
    surface = KrishnaBlueSurface,
    onPrimary = Color.White,
    onBackground = KrishnaBlueOnBackground,
    onSurface = KrishnaBlueOnSurface,
    secondaryContainer = KrishnaBlueAccent,
    onSecondaryContainer = KrishnaBlueOnBackground
)

private val ShivaGreyColorScheme = lightColorScheme(
    primary = ShivaGreyPrimary,
    background = ShivaGreyBackground,
    surface = ShivaGreySurface,
    onPrimary = Color.White,
    onBackground = ShivaGreyOnBackground,
    onSurface = ShivaGreyOnSurface,
    secondaryContainer = ShivaGreyAccent,
    onSecondaryContainer = ShivaGreyOnBackground
)

private val GayatriGoldColorScheme = lightColorScheme(
    primary = GayatriGoldPrimary,
    background = GayatriGoldBackground,
    surface = GayatriGoldSurface,
    onPrimary = Color.White,
    onBackground = GayatriGoldOnBackground,
    onSurface = GayatriGoldOnSurface,
    secondaryContainer = GayatriGoldAccent,
    onSecondaryContainer = GayatriGoldOnBackground
)

private val ClassicDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    secondaryContainer = DarkAccent,
    onSecondaryContainer = DarkOnBackground
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    background = AmoledBackground,
    surface = AmoledSurface,
    onPrimary = Color.Black,
    onBackground = AmoledOnBackground,
    onSurface = AmoledOnSurface,
    secondaryContainer = AmoledAccent,
    onSecondaryContainer = AmoledOnBackground
)

@Composable
fun MyApplicationTheme(
    activeTheme: String = "RamOrange",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (activeTheme) {
        "Light" -> RamOrangeColorScheme.copy(
            background = Color(0xFFF8F9FA),
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF212529),
            onSurface = Color(0xFF212529)
        )
        "Dark" -> ClassicDarkColorScheme
        "KrishnaBlue" -> KrishnaBlueColorScheme
        "ShivaGrey" -> ShivaGreyColorScheme
        "RamOrange" -> RamOrangeColorScheme
        "AmoledBlack" -> AmoledColorScheme
        else -> if (darkTheme) ClassicDarkColorScheme else RamOrangeColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
