package dk.michael.c25k.ui.activerun

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dk.michael.c25k.service.ActiveRunInfo
import dk.michael.c25k.ui.formatClock
import dk.michael.c25k.ui.theme.C25KPalette

@Composable
fun ActiveRunBanner(
    info: ActiveRunInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(18.dp, shape, clip = false)
            .clickable(onClick = onClick),
        shape = shape,
        color = C25KPalette.Surface,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(C25KPalette.Fjord),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Run", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aktiv løbetur",
                        color = C25KPalette.TextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${info.title} · ${info.currentStep} · ${formatClock(info.remainingSeconds)} tilbage",
                        color = C25KPalette.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Åbn",
                    color = C25KPalette.FjordDeep,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(C25KPalette.Accent)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(C25KPalette.SurfaceTint)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(info.progress)
                        .height(5.dp)
                        .background(C25KPalette.Accent)
                )
            }
        }
    }
}
