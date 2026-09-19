package dk.michael.c25k.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity
import dk.michael.c25k.data.model.Program
import dk.michael.c25k.ui.summaryText
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CancelledRedText
import dk.michael.c25k.ui.theme.CompletedGreen
import dk.michael.c25k.ui.theme.CompletedGreenText

@Composable
fun HomeScreen(onStartRun: () -> Unit, onOpenHistory: () -> Unit) {
    val viewModel: HomeViewModel = viewModel()
    val sessions by viewModel.sessions.collectAsState()
    val programs = viewModel.programs

    // dao returns newest-first, so the first hit per index is that program's latest outcome
    val latestOutcomeByIndex = remember(sessions) {
        val map = mutableMapOf<Int, RunOutcome>()
        for (s in sessions) map.putIfAbsent(s.programIndex, s.outcome)
        map
    }

    val graphPoints = remember(sessions) {
        sessions.sortedBy { it.dateTimeEpochMillis }
    }

    var selectedProgram by remember { mutableStateOf<Program?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Fremgang", style = MaterialTheme.typography.titleMedium)
        ProgressGraph(points = graphPoints, modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp))

        Text("Kalender", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(programs) { program ->
                val outcome = latestOutcomeByIndex[program.index]
                CalendarCell(program = program, outcome = outcome) {
                    selectedProgram = program
                }
            }
        }

        OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
            Text("Historik")
        }
        Button(onClick = onStartRun, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) {
            Text("Start løb")
        }
    }

    selectedProgram?.let { program ->
        AlertDialog(
            onDismissRequest = { selectedProgram = null },
            confirmButton = {
                Button(onClick = { selectedProgram = null }) { Text("Luk") }
            },
            title = { Text("Uge ${program.week} · dag ${program.day}") },
            text = { Text(program.summaryText() + " (plus 5 min opvarmning og nedkøling)") }
        )
    }
}

@Composable
private fun CalendarCell(program: Program, outcome: RunOutcome?, onClick: () -> Unit) {
    val (bg, fg) = when (outcome) {
        RunOutcome.COMPLETED -> CompletedGreen to CompletedGreenText
        RunOutcome.CANCELLED -> CancelledRed to CancelledRedText
        null -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(bg, RoundedCornerShape(6.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Uge ${program.week}", color = fg, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            Text("Dag ${program.day}", color = fg, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProgressGraph(points: List<RunSessionEntity>, modifier: Modifier = Modifier) {
    if (points.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Løb et par gange for at se din fremgang her", style = MaterialTheme.typography.bodySmall)
        }
        return
    }
    val maxIndex = points.maxOf { it.programIndex }.coerceAtLeast(1)
    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val stepX = size.width / (points.size - 1)
        val path = androidx.compose.ui.graphics.Path()
        points.forEachIndexed { i, session ->
            val x = i * stepX
            val y = size.height - (session.programIndex.toFloat() / maxIndex) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
    }
}
