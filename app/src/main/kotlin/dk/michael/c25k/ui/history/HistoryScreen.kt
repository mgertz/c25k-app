package dk.michael.c25k.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CancelledRedText
import dk.michael.c25k.ui.theme.CompletedGreen
import dk.michael.c25k.ui.theme.CompletedGreenText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@Composable
fun HistoryScreen(onOpenDetail: (Long) -> Unit) {
    val viewModel: HistoryViewModel = viewModel()
    val sessions by viewModel.sessions.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Historik", style = MaterialTheme.typography.titleLarge)
        LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
            items(sessions) { session ->
                HistoryRow(session = session, program = viewModel.program(session.programIndex)) {
                    onOpenDetail(session.id)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(session: RunSessionEntity, program: dk.michael.c25k.data.model.Program?, onClick: () -> Unit) {
    val (bg, fg) = when (session.outcome) {
        RunOutcome.COMPLETED -> CompletedGreen to CompletedGreenText
        RunOutcome.CANCELLED -> CancelledRed to CancelledRedText
    }
    val dateText = Instant.ofEpochMilli(session.dateTimeEpochMillis)
        .atZone(ZoneId.systemDefault())
        .format(dateFormatter)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(dateText, style = MaterialTheme.typography.bodyMedium)
            program?.let { Text("Uge ${it.week} · dag ${it.day}", style = MaterialTheme.typography.bodySmall) }
        }
        Text(
            text = if (session.outcome == RunOutcome.COMPLETED) "Gennemført" else "Afbrudt",
            color = fg,
            modifier = Modifier
                .background(bg, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
