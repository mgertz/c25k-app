package dk.michael.c25k.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity
import dk.michael.c25k.data.model.Program
import dk.michael.c25k.ui.navigation.BottomTab
import dk.michael.c25k.ui.navigation.C25KBottomBar
import dk.michael.c25k.ui.theme.C25KPalette
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CancelledRedText
import dk.michael.c25k.ui.theme.CompletedGreen
import dk.michael.c25k.ui.theme.CompletedGreenText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun HistoryScreen(onOpenDetail: (Long) -> Unit, onOpenActivity: () -> Unit = {}) {
    val viewModel: HistoryViewModel = viewModel()
    val sessions by viewModel.sessions.collectAsState()
    val completedCount = sessions.count { it.outcome == RunOutcome.COMPLETED }
    val cancelledCount = sessions.count { it.outcome == RunOutcome.CANCELLED }

    Scaffold(
        bottomBar = {
            C25KBottomBar(
                selected = BottomTab.HISTORY,
                onActivity = onOpenActivity,
                onHistory = {}
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(C25KPalette.FjordLight, C25KPalette.Fjord, C25KPalette.Background, C25KPalette.Background),
                        startY = 0f,
                        endY = 900f
                    )
                )
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 22.dp)
                ) {
                    Text(
                        text = "Historik",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Din C25K-logbog med gennemførte og afbrudte ture.",
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HistoryStat("Ture", sessions.size.toString(), Modifier.weight(1f))
                        HistoryStat("Gennemført", completedCount.toString(), Modifier.weight(1f))
                        HistoryStat("Afbrudt", cancelledCount.toString(), Modifier.weight(1f))
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                    color = C25KPalette.Background
                ) {
                    if (sessions.isEmpty()) {
                        EmptyHistory()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sessions, key = { it.id }) { session ->
                                HistoryCard(
                                    session = session,
                                    program = viewModel.program(session.programIndex),
                                    onClick = { onOpenDetail(session.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStat(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, color = Color.White.copy(alpha = 0.68f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HistoryCard(session: RunSessionEntity, program: Program?, onClick: () -> Unit) {
    val dateTime = Instant.ofEpochMilli(session.dateTimeEpochMillis).atZone(ZoneId.systemDefault())
    val title = program?.let { "Uge ${it.week} · dag ${it.day}" } ?: "Workout ${session.programIndex + 1}"
    val (chipText, chipBg, chipFg) = when (session.outcome) {
        RunOutcome.COMPLETED -> Triple("Gennemført", CompletedGreen, CompletedGreenText)
        RunOutcome.CANCELLED -> Triple("Afbrudt", CancelledRed, CancelledRedText)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = C25KPalette.Surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateBadge(day = dateTime.dayOfMonth.toString(), month = dateTime.month.name.take(3))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = C25KPalette.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = chipText,
                        color = chipFg,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(chipBg)
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
                Text(
                    text = "${dateTime.format(dateFormatter)} kl. ${dateTime.format(timeFormatter)}",
                    color = C25KPalette.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnergyMini("Før", session.energyBefore)
                    EnergyMini("Efter", session.energyAfter)
                }
                if (session.note.isNotBlank()) {
                    Text(
                        text = session.note,
                        color = C25KPalette.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBadge(day: String, month: String) {
    Column(
        modifier = Modifier
            .height(70.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(C25KPalette.SurfaceTint)
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(day, color = C25KPalette.Fjord, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(month.lowercase().replaceFirstChar { it.uppercase() }, color = C25KPalette.TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EnergyMini(label: String, value: Int) {
    val text = if (value > 0) "$label $value/5" else "$label -"
    Text(
        text = text,
        color = C25KPalette.Fjord,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(C25KPalette.SurfaceTint)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    )
}

@Composable
private fun EmptyHistory() {
    Box(modifier = Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ingen ture endnu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = C25KPalette.TextPrimary)
            Text(
                text = "Når du har gennemført eller afbrudt et løb, dukker det op her.",
                style = MaterialTheme.typography.bodyMedium,
                color = C25KPalette.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
