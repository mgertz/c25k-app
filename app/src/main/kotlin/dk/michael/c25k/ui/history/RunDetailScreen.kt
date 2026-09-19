package dk.michael.c25k.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity
import dk.michael.c25k.ui.StarRow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@Composable
fun RunDetailScreen(sessionId: Long) {
    val viewModel: HistoryViewModel = viewModel()
    var session by remember { mutableStateOf<RunSessionEntity?>(null) }

    LaunchedEffect(sessionId) { session = viewModel.byId(sessionId) }

    val current = session ?: return
    val program = viewModel.program(current.programIndex)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Uge ${program?.week} · dag ${program?.day}", style = MaterialTheme.typography.titleLarge)
        Text(
            Instant.ofEpochMilli(current.dateTimeEpochMillis).atZone(ZoneId.systemDefault()).format(dateFormatter),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            if (current.outcome == RunOutcome.COMPLETED) "Gennemført" else "Afbrudt",
            modifier = Modifier.padding(top = 4.dp)
        )

        Text("Energi før", modifier = Modifier.padding(top = 16.dp))
        StarRow(value = current.energyBefore)

        Text("Energi efter", modifier = Modifier.padding(top = 8.dp))
        StarRow(value = current.energyAfter)

        if (current.note.isNotBlank()) {
            Text("Note", modifier = Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleSmall)
            Text(current.note)
        }
    }
}
