package dk.michael.c25k.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity
import dk.michael.c25k.data.model.Program
import dk.michael.c25k.ui.C25KBackButton
import dk.michael.c25k.ui.formatClock
import dk.michael.c25k.ui.runSeconds
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CancelledRedText
import dk.michael.c25k.ui.theme.CompletedGreen
import dk.michael.c25k.ui.theme.CompletedGreenText
import dk.michael.c25k.ui.totalSeconds
import dk.michael.c25k.ui.walkSeconds
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val detailDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@Composable
fun RunDetailScreen(sessionId: Long, onBack: () -> Unit = {}) {
    val viewModel: HistoryViewModel = viewModel()
    var session by remember { mutableStateOf<RunSessionEntity?>(null) }

    LaunchedEffect(sessionId) { session = viewModel.byId(sessionId) }

    val current = session
    if (current == null) {
        LoadingDetail(onBack = onBack)
        return
    }

    val program = viewModel.program(current.programIndex)
    val dateText = Instant.ofEpochMilli(current.dateTimeEpochMillis)
        .atZone(ZoneId.systemDefault())
        .format(detailDateFormatter)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F8FA))
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                DetailHeader(current = current, program = program, dateText = dateText, onBack = onBack)
            }
            item {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EnergyDetailCard("Energi før", current.energyBefore, Modifier.weight(1f))
                        EnergyDetailCard("Energi efter", current.energyAfter, Modifier.weight(1f))
                    }

                    program?.let { WorkoutSummary(program = it) }

                    NoteCard(note = current.note)
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(current: RunSessionEntity, program: Program?, dateText: String, onBack: () -> Unit) {
    val title = program?.let { "Uge ${it.week} · dag ${it.day}" } ?: "Workout ${current.programIndex + 1}"
    val (statusText, statusBg, statusFg) = when (current.outcome) {
        RunOutcome.COMPLETED -> Triple("Gennemført", CompletedGreen, CompletedGreenText)
        RunOutcome.CANCELLED -> Triple("Afbrudt", CancelledRed, CancelledRedText)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF12B7C5), Color(0xFF087A9B), Color(0xFF073A60)))
            )
            .padding(horizontal = 18.dp, vertical = 22.dp)
    ) {
        C25KBackButton(onClick = onBack)
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp)
        )
        Text(
            text = dateText,
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = statusText,
            color = statusFg,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 18.dp, bottom = 10.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(statusBg)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun EnergyDetailCard(title: String, value: Int, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(118.dp), shape = RoundedCornerShape(26.dp), color = Color.White, tonalElevation = 3.dp) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = Color(0xFF647B84))
            Text(
                text = if (value > 0) "$value/5" else "-",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF087A9B),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            StarLine(value = value)
        }
    }
}

@Composable
private fun StarLine(value: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.padding(top = 4.dp)) {
        for (i in 1..5) {
            Text(
                text = if (i <= value) "★" else "☆",
                color = if (i <= value) Color(0xFF0A88B0) else Color(0xFFB6C4CA),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun WorkoutSummary(program: Program) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color.White, tonalElevation = 3.dp) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Workout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF203840))
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric("Total", formatClock(program.totalSeconds()), Modifier.weight(1f))
                SummaryMetric("Løb", formatClock(program.runSeconds()), Modifier.weight(1f))
                SummaryMetric("Gå", formatClock(program.walkSeconds()), Modifier.weight(1f))
            }
            Text(
                text = "${program.intervals.size} intervaller · ${formatClock(program.warmupSeconds)} opvarmning · ${formatClock(program.cooldownSeconds)} nedkøling",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF647B84),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun SummaryMetric(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFEAF8FA))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color(0xFF087A9B), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(title, color = Color(0xFF647B84), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

@Composable
private fun NoteCard(note: String) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color.White, tonalElevation = 3.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text("Note", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF203840))
            Text(
                text = note.ifBlank { "Ingen note gemt for denne tur." },
                style = MaterialTheme.typography.bodyMedium,
                color = if (note.isBlank()) Color(0xFF8A9BA2) else Color(0xFF415860),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun LoadingDetail(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Henter løb...", style = MaterialTheme.typography.titleMedium)
            C25KBackButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
