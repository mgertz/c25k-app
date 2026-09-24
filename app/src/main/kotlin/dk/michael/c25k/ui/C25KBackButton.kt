package dk.michael.c25k.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun C25KBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Tilbage"
) {
    val shape = RoundedCornerShape(99.dp)
    Row(
        modifier = modifier
            .shadow(8.dp, shape, clip = false)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.96f))
            .clickable(onClick = onClick)
            .height(44.dp)
            .padding(start = 6.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(shape)
                .background(Color(0xFFE8F8FA)),
            contentAlignment = Alignment.Center
        ) {
            BackArrowIcon(color = Color(0xFF064263))
        }
        Text(
            text = text,
            color = Color(0xFF064263),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun BackArrowIcon(color: Color) {
    Canvas(modifier = Modifier.size(17.dp)) {
        val stroke = 2.2.dp.toPx()
        val centerY = size.height / 2f
        val left = Offset(size.width * 0.22f, centerY)

        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.2f),
            end = left,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = left,
            end = Offset(size.width * 0.58f, size.height * 0.8f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = left,
            end = Offset(size.width * 0.84f, centerY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
