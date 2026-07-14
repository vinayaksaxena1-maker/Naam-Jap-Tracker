package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Canvas-based Digital Mala rendering.
 * Renders a circular bead chain with a traditional tassel at the bottom.
 * The active bead has a glowing highlight.
 */
@Composable
fun DigitalMala(
    totalBeads: Int = 54,
    currentBead: Int,
    onBeadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBead = animateFloatAsState(targetValue = currentBead.toFloat(), label = "bead_anim")
    
    // Rudraksha bead colors
    val beadDarkBrown = Color(0xFF5D4037)
    val beadMediumBrown = Color(0xFF8D6E63)
    val beadLightBrown = Color(0xFFD7CCC8)
    val activeGlow = Color(0xFFFF9800)
    val tasselColor = Color(0xFFD35400)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onBeadClick()
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2.5f

        // 1. Draw Tassel at the bottom (usually angle 90 degrees or -90 depending on starting point)
        // We place the tassel at the very bottom center (angle 90 degrees)
        val tasselAngleRad = Math.toRadians(90.0).toFloat()
        val tasselX = center.x + radius * cos(tasselAngleRad)
        val tasselY = center.y + radius * sin(tasselAngleRad)

        // Draw tassel knot
        drawCircle(
            color = Color(0xFFA04000),
            radius = 14f,
            center = Offset(tasselX, tasselY)
        )
        // Draw tassel fringe
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(tasselX, tasselY)
            lineTo(tasselX - 25f, tasselY + 60f)
            lineTo(tasselX + 25f, tasselY + 60f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(tasselColor, Color(0xFFB03A2E)),
                startY = tasselY,
                endY = tasselY + 60f
            )
        )

        // Draw connecting string
        drawCircle(
            color = beadDarkBrown.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 4f)
        )

        // 2. Draw Beads
        for (i in 0 until totalBeads) {
            // Distribute beads around the circle, leaving a small gap near the tassel (angle 90 deg)
            val angleDeg = (i * 360f / totalBeads) - 90f
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()

            val beadX = center.x + radius * cos(angleRad)
            val beadY = center.y + radius * sin(angleRad)

            val isBeadActive = i == (currentBead % totalBeads)

            // Bead base gradient (Rudraksha bead depth)
            val beadBrush = Brush.radialGradient(
                colors = listOf(beadLightBrown, beadMediumBrown, beadDarkBrown),
                center = Offset(beadX - 4f, beadY - 4f),
                radius = 15f
            )

            // Draw bead shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = 16f,
                center = Offset(beadX + 2f, beadY + 3f)
            )

            // Draw actual bead
            drawCircle(
                brush = beadBrush,
                radius = 15f,
                center = Offset(beadX, beadY)
            )

            // Draw active bead indicator
            if (isBeadActive) {
                // Outer glow ring
                drawCircle(
                    color = activeGlow,
                    radius = 20f,
                    center = Offset(beadX, beadY),
                    style = Stroke(width = 4f)
                )
                // Core bright center
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(beadX, beadY)
                )
            }
        }
    }
}

/**
 * Beautiful Custom Circular Progress Indicator for Goals.
 */
@Composable
fun GoalProgressCircle(
    progress: Float, // 0f to 1f
    displayText: String,
    subText: String,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.outline,
    modifier: Modifier = Modifier
) {
    val animatedProgress = animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "progress")

    Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) / 2f - 16f

            // Background circle track
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = 16f)
            )

            // Foreground progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }

        // Center labels
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                text = displayText,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.material3.Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * High-fidelity Custom Bar Chart rendering rounded pill bars, grid lines, labels and dynamic limits.
 */
@Composable
fun CustomBarChart(
    data: List<Pair<String, Float>>, // Pair(Label, Value)
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val textPaintColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    val textDensity = LocalDensity.current
    val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)

    Canvas(modifier = modifier) {
        if (data.isEmpty()) {
            return@Canvas
        }

        val paddingLeft = 60f
        val paddingBottom = 60f
        val paddingTop = 20f
        val paddingRight = 20f

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val maxValue = (data.maxOf { it.second }).coerceAtLeast(108f)
        val stepCount = 4
        
        // Draw grid lines and y-axis labels
        for (i in 0..stepCount) {
            val yValue = maxValue * i / stepCount
            val yPos = size.height - paddingBottom - (chartHeight * i / stepCount)

            // Grid line
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, yPos),
                end = Offset(size.width - paddingRight, yPos),
                strokeWidth = 2f
            )

            // Y-axis Label
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.0f", yValue),
                10f,
                yPos + 10f,
                android.graphics.Paint().apply {
                    color = textPaintColor.value.toInt()
                    textSize = with(textDensity) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }

        // Draw bars and x-axis labels
        val barCount = data.size
        val barSpacing = chartWidth / barCount
        val barWidth = barSpacing * 0.5f

        data.forEachIndexed { index, pair ->
            val barHeight = if (maxValue > 0) (pair.second / maxValue) * chartHeight else 0f
            val xPos = paddingLeft + (index * barSpacing) + (barSpacing - barWidth) / 2f
            val yPos = size.height - paddingBottom - barHeight

            // Draw the rounded pill bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(xPos, yPos),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // X-axis label
            drawContext.canvas.nativeCanvas.drawText(
                pair.first,
                xPos + barWidth / 2f,
                size.height - 15f,
                android.graphics.Paint().apply {
                    color = textPaintColor.value.toInt()
                    textSize = with(textDensity) { 11.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}
