package dk.michael.c25k.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class BottomTab { ACTIVITY, HISTORY }

@Composable
fun C25KBottomBar(
    selected: BottomTab,
    onActivity: () -> Unit,
    onHistory: () -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFFD9FF55),
        selectedTextColor = Color.White,
        indicatorColor = Color.White.copy(alpha = 0.12f),
        unselectedIconColor = Color.White.copy(alpha = 0.55f),
        unselectedTextColor = Color.White.copy(alpha = 0.55f)
    )

    NavigationBar(containerColor = Color(0xFF064263), tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = selected == BottomTab.ACTIVITY,
            onClick = onActivity,
            icon = { StopwatchIcon() },
            label = { Text("Run") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selected == BottomTab.HISTORY,
            onClick = onHistory,
            icon = { HistoryGraphIcon() },
            label = { Text("Historik") },
            colors = itemColors
        )
    }
}

@Composable
private fun StopwatchIcon() {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = 2.2.dp.toPx()
        val center = Offset(size.width / 2f, size.height * 0.58f)
        val radius = size.minDimension * 0.34f

        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawLine(color, Offset(size.width / 2f, 2.dp.toPx()), Offset(size.width / 2f, 5.5.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.37f, 5.dp.toPx()), Offset(size.width * 0.29f, 7.4.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, center, Offset(center.x, center.y - radius * 0.48f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, center, Offset(center.x + radius * 0.45f, center.y), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun HistoryGraphIcon() {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = 2.4.dp.toPx()
        val points = listOf(
            Offset(size.width * 0.14f, size.height * 0.72f),
            Offset(size.width * 0.34f, size.height * 0.54f),
            Offset(size.width * 0.50f, size.height * 0.62f),
            Offset(size.width * 0.70f, size.height * 0.32f),
            Offset(size.width * 0.88f, size.height * 0.40f)
        )

        points.zipWithNext().forEach { (from, to) ->
            drawLine(color, from, to, strokeWidth = stroke, cap = StrokeCap.Round)
        }
        points.forEach { point ->
            drawCircle(color = color, radius = 1.9.dp.toPx(), center = point)
        }
    }
}
