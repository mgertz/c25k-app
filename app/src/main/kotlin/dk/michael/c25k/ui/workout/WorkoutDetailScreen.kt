package dk.michael.c25k.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.model.IntervalStep
import dk.michael.c25k.data.model.Program
import dk.michael.c25k.ui.C25KBackButton
import dk.michael.c25k.ui.formatClock
import dk.michael.c25k.ui.formatDuration
import dk.michael.c25k.ui.home.HomeViewModel
import dk.michael.c25k.ui.runSeconds
import dk.michael.c25k.ui.theme.C25KPalette
import dk.michael.c25k.ui.totalSeconds
import dk.michael.c25k.ui.walkSeconds

@Composable
fun WorkoutDetailScreen(programIndex: Int, onStartRun: (Int) -> Unit, onBack: () -> Unit) {
    val viewModel: HomeViewModel = viewModel()
    val program = viewModel.programs.firstOrNull { it.index == programIndex }

    if (program == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Workout blev ikke fundet", style = MaterialTheme.typography.titleLarge)
            C25KBackButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C25KPalette.Background)
    ) {
        WorkoutHeader(program = program, onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailStat("Total", formatClock(program.totalSeconds()), Modifier.weight(1f))
                    DetailStat("Løb", formatClock(program.runSeconds()), Modifier.weight(1f))
                    DetailStat("Gå", formatClock(program.walkSeconds()), Modifier.weight(1f))
                }
            }

            item { SectionRow("Opvarmning", formatDuration(program.warmupSeconds), "Start roligt og få kroppen klar.") }
            itemsIndexed(program.intervals) { index, interval ->
                IntervalRow(index = index + 1, interval = interval)
            }
            item { SectionRow("Nedkøling", formatDuration(program.cooldownSeconds), "Gå pulsen ned og afslut kontrolleret.") }
        }

        Button(
            onClick = { onStartRun(program.index) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(54.dp)
        ) {
            Text("Start løb")
        }
    }
}

@Composable
private fun WorkoutHeader(program: Program, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(C25KPalette.FjordLight, C25KPalette.Fjord)
                )
            )
            .padding(16.dp)
    ) {
        C25KBackButton(onClick = onBack)
        Text(
            text = "Uge ${program.week} · dag ${program.day}",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = "Dagens workout er bygget op af opvarmning, intervaller og nedkøling.",
            color = Color.White.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun DetailStat(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = C25KPalette.Surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = C25KPalette.Fjord, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = C25KPalette.TextSecondary)
        }
    }
}

@Composable
private fun SectionRow(title: String, duration: String, subtitle: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = C25KPalette.Surface, tonalElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = C25KPalette.TextSecondary)
            }
            Text(duration, color = C25KPalette.Fjord, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IntervalRow(index: Int, interval: IntervalStep) {
    val isRun = interval.type == "run"
    val title = if (isRun) "Løb" else "Gå"
    val accent = if (isRun) C25KPalette.Run else C25KPalette.Walk

    Surface(shape = RoundedCornerShape(14.dp), color = C25KPalette.Surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = index.toString(),
                    color = Color.White,
                    modifier = Modifier
                        .background(accent, RoundedCornerShape(99.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(if (isRun) "Hold et kontrolleret tempo" else "Find ro i vejrtrækningen", style = MaterialTheme.typography.bodySmall, color = C25KPalette.TextSecondary)
                }
            }
            Text(formatDuration(interval.seconds), color = accent, fontWeight = FontWeight.Bold)
        }
    }
}
