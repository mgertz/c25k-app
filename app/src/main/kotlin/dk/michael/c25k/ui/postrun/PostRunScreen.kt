package dk.michael.c25k.ui.postrun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.ui.StarRow

@Composable
fun PostRunScreen(sessionId: Long, onSaved: () -> Unit) {
    val viewModel: PostRunViewModel = viewModel()
    LaunchedEffect(sessionId) { viewModel.load(sessionId) }
    val session by viewModel.session.collectAsState()

    var note by remember { mutableStateOf("") }
    var energyBefore by remember { mutableStateOf(3) }
    var energyAfter by remember { mutableStateOf(3) }
    var outcome by remember { mutableStateOf<RunOutcome?>(null) }

    LaunchedEffect(session?.id) {
        session?.let {
            note = it.note
            energyBefore = it.energyBefore.takeIf { value -> value > 0 } ?: 3
            energyAfter = it.energyAfter.takeIf { value -> value > 0 } ?: 3
            outcome = it.outcome
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Efter løbet", style = MaterialTheme.typography.titleLarge)

        Text("Hvordan gik det?", modifier = Modifier.padding(top = 12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutcomeButton(
                text = "Gennemført",
                selected = outcome == RunOutcome.COMPLETED,
                onClick = { outcome = RunOutcome.COMPLETED },
                modifier = Modifier.weight(1f)
            )
            OutcomeButton(
                text = "Afbrudt",
                selected = outcome == RunOutcome.CANCELLED,
                onClick = { outcome = RunOutcome.CANCELLED },
                modifier = Modifier.weight(1f)
            )
        }

        Text("Energi før løbet", modifier = Modifier.padding(top = 12.dp))
        StarRow(value = energyBefore) { energyBefore = it }

        Text("Energi efter løbet", modifier = Modifier.padding(top = 12.dp))
        StarRow(value = energyAfter) { energyAfter = it }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 12.dp)
        )

        Button(
            onClick = { outcome?.let { viewModel.save(sessionId, it, note, energyBefore, energyAfter, onSaved) } },
            enabled = outcome != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Gem")
        }
    }
}

@Composable
private fun OutcomeButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) }
    }
}
